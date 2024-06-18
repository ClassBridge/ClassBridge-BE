package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.REFUND_NOT_FOUND;

import com.linked.classbridge.config.PayProperties;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.Refund;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.refund.PaymentRefundDto;
import com.linked.classbridge.dto.refund.PaymentRefundDto.Requset;
import com.linked.classbridge.dto.refund.PaymentRefundDto.Response;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import com.linked.classbridge.dto.reservation.ReservationStatus;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.PaymentRepository;
import com.linked.classbridge.repository.RefundRepository;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.util.RefundPolicyUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoRefundService {
    private final PayProperties payProperties;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final LessonService lessonService;

    /**
     * 결제 환불
     */
    @Transactional
    public PaymentRefundDto.Response refundPayment(PaymentRefundDto.Requset request,
                                                   Authentication authentication) {

        // 취소할 결제
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RestApiException(ErrorCode.INVALID_PAYMENT_ID));

        // 연관된 예약
        Reservation reservation = payment.getReservation();
        if (reservation == null) {
            throw new RestApiException(ErrorCode.INVALID_RESERVATION_ID);
        }

        // 환불 비율 계산
        double refundRate = RefundPolicyUtils.calculateRefundRate(reservation.getLesson().getLessonDate(),
                reservation.getLesson().getStartTime(),
                LocalDateTime.now());

        // 카카오 결제 취소에 필요한 파라미터
        Map<String, String> parameters = getRefundParameters(request, refundRate, payment);

        // 결제 취소 요청 준비(헤더 세팅)
        WebClient webClient = WebClient.builder()
                .baseUrl(payProperties.getCancelUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "SECRET_KEY " + payProperties.getDevKey())
                .build();

        // 결제 취소 요청
        Optional<Response> refundResponseData = Optional.ofNullable(webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/").build())
                .bodyValue(parameters)
//                .retrieve()
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is5xxServerError()) {
                        return Mono.error(new RestApiException(ErrorCode.PAY_ERROR));
                    } else {
                        return clientResponse.bodyToMono(Response.class);
                    }
                })
//                .bodyToMono(Response.class)
                .block());

        // 취소 응답
        PaymentRefundDto.Response response = refundResponseData.orElseThrow(
                () -> new RestApiException(ErrorCode.PAY_ERROR));

        log.info("response refund status :: {}", response.getStatus());


        // 예약 status 조회

        // 부분 환불의 경우에만 수량을 입력하고, 전체 환불일 경우 수량이 null 이므로 처리
        int refundQuantity = Optional.ofNullable(request.getQuantity()).orElse(0);

        // 부분환불 n번 => 전체환불이 되는 경우
        if (payment.getQuantity() == refundQuantity
                && request.getRefundType() == PaymentStatusType.PARTIAL_REFUND) {

            payment.setStatus(PaymentStatusType.REFUNDED);
            // 예약도 취소상태로
            reservation.setStatus(ReservationStatus.CANCELED_BY_CUSTOMER);
        }

        // 부분 환불의 경우 Refund 테이블에 데이터 추가
        if(request.getRefundType() == PaymentStatusType.PARTIAL_REFUND) {
            // 수량 업데이트
            calculatePaymentQuantity(payment, refundQuantity);
            // 결제 상태 부분 환불로 변경
            payment.setStatus(PaymentStatusType.PARTIAL_REFUND);
            // 결제 금액 업데이트
            int newTotalAmount = calculateNewTotalAmount(payment, request.getCancelAmount());
            payment.setTotalAmount(newTotalAmount);
            // 환불 정보
            Refund refund = getRefund(request, payment, response);
            refundRepository.save(refund);
        } else if (request.getRefundType() == PaymentStatusType.REFUNDED) {
            refundQuantity = reservation.getQuantity();
            // 결제 상태 환불로 변경
            payment.setStatus(PaymentStatusType.REFUNDED);
            // 예약 상태 취소로 변경
            reservation.setStatus(ReservationStatus.CANCELED_BY_CUSTOMER);
        }

        // 클래스 lesson 참여 인원 수정
        lessonService.updateParticipantCount(reservation.getLesson(), refundQuantity);

        return response;

    }

    @NotNull
    private static Refund getRefund(Requset request, Payment payment, Response response) {
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setQuantity(request.getQuantity());
        refund.setAmount(response.getAmount().getTotal());
        refund.setApprovedCancelAmount(response.getApproved_cancel_amount().getTotal());
        refund.setCanceledAmount(response.getApproved_cancel_amount().getTotal());
        refund.setCancelAvailableAmount(response.getCancel_available_amount().getTotal());
        refund.setStatus(PaymentStatusType.PARTIAL_REFUND);
        refund.setCanceled_at(response.getCanceled_at());
        refund.setApproved_at(response.getApproved_at());
        return refund;
    }

    @NotNull
    private Map<String, String> getRefundParameters(Requset request, double refundRate, Payment payment) {
        int refundAmount = (int) (request.getCancelAmount() * refundRate);

        if (refundAmount == 0) {
            throw new RestApiException(ErrorCode.NO_REFUND_AVAILABLE);
        }
        // 카카오페이 요청
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", payProperties.getCid());
        parameters.put("tid", payment.getTid());
        parameters.put("cancel_amount", String.valueOf(refundAmount));
        parameters.put("cancel_tax_free_amount", String.valueOf(0));
        return parameters;
    }

    private void calculatePaymentQuantity(Payment payment, int refundQuantity) {
        payment.calculateQuantity(refundQuantity);
    }

    private int calculateNewTotalAmount(Payment payment, int cancelAmount) {
        return payment.getTotalAmount() - cancelAmount;
    }

    @Transactional(readOnly = true)
    public List<PaymentRefundDto> getAllRefunds() {
        List<Refund> refunds = refundRepository.findAll();
        return refunds.stream()
                .map(PaymentRefundDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentRefundDto getRefundById(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RestApiException(REFUND_NOT_FOUND));
        return PaymentRefundDto.from(refund);
    }
}
