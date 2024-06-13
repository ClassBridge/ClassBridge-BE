package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.LESSON_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.MAX_PARTICIPANTS_EXCEEDED;
import static com.linked.classbridge.type.ErrorCode.RESERVATION_NOT_FOUND;

import com.linked.classbridge.config.PayProperties;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.payment.CreatePaymentResponse;
import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Response;
import com.linked.classbridge.dto.reservation.ReservationStatus;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.PaymentRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * 결제 요청 생성 결제 승인 결제 취소 결제 상태 조회
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoPaymentService {

    private final PayProperties payProperties;
    private final WebClient.Builder webClient;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final LessonRepository lessonRepository;
    private final LessonService lessonService;

    /**
     * 카카오페이 결제 요청 로직
     */
//    @Override
    @Transactional
    public PaymentPrepareDto.Response initiatePayment(Request request) {

        Long reservationId = request.getReservationId();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RestApiException(RESERVATION_NOT_FOUND));

        Long lessonId = reservation.getLesson().getLessonId();
        // 수용 인원 제한 확인
        validateLessonCapacity(lessonId, request.getQuantity());

        // 수용 인원 사전 예약
        preReserveSeats(lessonId, request.getQuantity());

        // 카카오페이 요청 형식
        Map<String, String> parameters = getInitiateParameters(request);

        try {
            return webClient.build().post()
                    .uri(payProperties.getReadyUrl())
                    .headers(headers -> {
                        headers.setAll(getHeaders().toSingleValueMap());
                    })
                    .bodyValue(parameters)
                    .retrieve()
                    .bodyToMono(Response.class)
                    .block();// 동기식 처리

        } catch (WebClientResponseException e) {
            throw new RestApiException(ErrorCode.PAY_ERROR);
        }
    }

    /**
     * 카카오페이 결제 승인 로직
     */
    public ResponseEntity<String> approvePayment(PaymentPrepareDto.Response response, String header) {
        try {
            // 카카오 요청
            Map<String, String> parameters = new HashMap<>();
            parameters.put("cid", payProperties.getCid());
            parameters.put("tid", response.getTid());
            parameters.put("partner_order_id", response.getPartnerOrderId());
            parameters.put("partner_user_id", response.getPartnerUserId());
            parameters.put("pg_token", response.getPgToken());
            parameters.put("item_name", response.getItemName());
            parameters.put("quantity", String.valueOf(response.getQuantity()));

//            response.setCid(payProperties.getCid());

            log.info("kakao payment tid :: {}", response.getTid());
            log.info("kakao payment pg token :: {}", response.getPgToken());

            WebClient webClient = WebClient.builder()
                    .baseUrl(payProperties.getApproveUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "SECRET_KEY " + payProperties.getDevKey())
                    .build();

            PaymentApproveDto.Response kakaoResponse = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/").build())
                    .bodyValue(parameters)
                    .retrieve()
                    .bodyToMono(PaymentApproveDto.Response.class)
                    .block();

            kakaoResponse.setReservationId(response.getReservationId());

            // POST 요청 생성
            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "SECRET_KEY " + payProperties.getDevKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            WebClient localWebClient = WebClient.builder()
                    .baseUrl("http://localhost:8080")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                    .defaultHeader(HttpHeaders.AUTHORIZATION, "SECRET_KEY " + payProperties.getDevKey())
                    .build();

            // 에러 확인을 위한 로깅 추가
            log.info("Sending request to /api/payments/complete with body: {}", kakaoResponse);

            Mono<ResponseEntity<String>> entity = localWebClient.post()
                    .uri("/api/payments/complete")
                    .bodyValue(kakaoResponse)
                    .retrieve()
                    .toEntity(String.class);

            return entity.block();

        } catch (WebClientResponseException e) {
            log.error(e.getMessage());
            throw new RestApiException(ErrorCode.PAY_ERROR);
        }
    }

    /**
     * 카카오페이 승인 결과 저장
     */
    public CreatePaymentResponse savePayment(PaymentApproveDto.Response response) {

        try {
            Payment payment = Payment.convertToPaymentEntity(response);
            Payment saved = paymentRepository.save(payment);

            // 예약도 확정
            Reservation reservation = confirmReservation(response.getReservationId(), payment);

            // Lesson 테이블 참여인원 업데이트
            updateLessonParticipantCount(reservation.getLesson().getLessonId(),
                    -response.getQuantity());

            return toCreatePaymentResponse(saved);

        } catch (OptimisticLockingFailureException e) {
            handleOptimisticLockingFailure(response.getReservationId(), response.getQuantity());
            throw new RestApiException(MAX_PARTICIPANTS_EXCEEDED);
        }
    }

    // 예약 확정
    private Reservation confirmReservation(Long reservationId, Payment payment) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RestApiException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setPayment(payment);
        return reservationRepository.save(reservation);
    }

    // 참여자 수 업데이트
    private void updateLessonParticipantCount(Long lessonId, int quantityChange) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RestApiException(ErrorCode.LESSON_NOT_FOUND));
        lessonService.updateParticipantCount(lesson, quantityChange);
    }

    // 실패 시 예약 좌석 취소
    private void handleOptimisticLockingFailure(Long reservationId, int quantity) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RestApiException(ErrorCode.RESERVATION_NOT_FOUND));
        Long lessonId = reservation.getLesson().getLessonId();
        releaseSeats(lessonId, quantity);
    }

    public CreatePaymentResponse toCreatePaymentResponse(Payment payment) {
        return new CreatePaymentResponse(payment.getPaymentId());
    }

    /**
     * 카카오페이 결제 요청 시 필요한 파라미터
     */
    private Map<String, String> getInitiateParameters(Request request) {

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        request.setPartnerUserId(userEmail);
        request.setPartnerOrderId(OrderNumberGenerator.generateOrderNumber(userEmail));

        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", payProperties.getCid());
        parameters.put("partner_order_id", request.getPartnerOrderId());
        parameters.put("partner_user_id", request.getPartnerUserId());
        parameters.put("item_name", request.getItemName());
        parameters.put("quantity", Integer.toString(request.getQuantity()));
        parameters.put("total_amount", Integer.toString(request.getTotalAmount()));
        parameters.put("tax_free_amount", Integer.toString(request.getTexFreeAmount()));
        parameters.put("approval_url", "http://localhost:8080/api/payments/complete"); // 성공 시 redirect url
        parameters.put("cancel_url", "http://localhost:8080/api/payments/cancel"); // 취소 시 redirect url
        parameters.put("fail_url", "http://localhost:8080/api/payments/fail"); // 실패 시 redirect url

        return parameters;
    }

    /**
     * 카카오페이 결제 시 필요한 헤더
     */
    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String auth = "SECRET_KEY " + payProperties.getDevKey();

        httpHeaders.set("Authorization", auth);
        httpHeaders.set("Content-type", "application/json");

        return httpHeaders;
    }

    // 가능 인원 확인
    private void validateLessonCapacity(Long lessonId, int requestedQuantity) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RestApiException(LESSON_NOT_FOUND));
        if (lesson.getAvailableSeats() < requestedQuantity) {
            throw new RestApiException(ErrorCode.MAX_PARTICIPANTS_EXCEEDED);
        }
    }

    // 인원 사전 예약
    private void preReserveSeats(Long lessonId, int quantity) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RestApiException(LESSON_NOT_FOUND));
        lesson.setParticipantNumber(lesson.getParticipantNumber() + quantity);
        lessonRepository.save(lesson);
    }

    private void releaseSeats(Long lessonId, int quantity) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RestApiException(LESSON_NOT_FOUND));
        lesson.setParticipantNumber(lesson.getParticipantNumber() - quantity);
        lessonRepository.save(lesson);
    }
}