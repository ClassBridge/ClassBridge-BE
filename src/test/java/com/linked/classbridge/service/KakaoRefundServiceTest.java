package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.REFUND_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.config.PayProperties;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.Refund;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.payment.KakaoStatusType;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import com.linked.classbridge.dto.refund.PaymentRefundDto;
import com.linked.classbridge.dto.refund.PaymentRefundDto.Response;
import com.linked.classbridge.type.ReservationStatus;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.PaymentRepository;
import com.linked.classbridge.repository.RefundRepository;
import com.linked.classbridge.type.ErrorCode;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
public class KakaoRefundServiceTest {
    @Mock
    private PayProperties payProperties;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private KakaoRefundService kakaoRefundService;

    @InjectMocks
    private KakaoRefundService refundService;
    private PaymentRefundDto.Requset request;
    private Payment payment;
    private Refund refund;
    private Reservation reservation;
    private Lesson lesson;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        request = new PaymentRefundDto.Requset();
        request.setPaymentId(1L);
        request.setCancelAmount(1000);
        request.setRefundType(PaymentStatusType.REFUNDED_BY_CUSTOMER);
        request.setQuantity(1);

        lesson = new Lesson();
        lesson.setLessonDate(LocalDateTime.now().plusDays(10).toLocalDate()); // 임의의 날짜 설정
        lesson.setStartTime(LocalTime.now().plusHours(1)); // 임의의 시간 설정

        reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setLesson(lesson);
        reservation.setQuantity(1); // 수량 설정

        payment = new Payment();
        payment.setReservation(reservation);
        payment.setTid("test_tid");
        payment.setTotalAmount(10000);
        payment.setQuantity(1); // 수량 설정

        refund = new Refund();
        refund.setRefundId(1L);
        refund.setStatus(PaymentStatusType.COMPLETED);
        refund.setAmount(1000);
        refund.setPayment(payment);

        lenient().when(payProperties.getCancelUrl()).thenReturn(baseUrl);
        lenient().when(payProperties.getDevKey()).thenReturn("devKey");
        lenient().when(payProperties.getCid()).thenReturn("test_cid");

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "SECRET_KEY " + payProperties.getDevKey())
                .build();

        kakaoRefundService = new KakaoRefundService(payProperties, paymentRepository, refundRepository, lessonService);
    }

    @AfterEach
    void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("환불 성공")
    void refundPayment_Success() {
        when(paymentRepository.findById(request.getPaymentId())).thenReturn(Optional.of(payment));

        Response mockResponse = new Response();
        mockResponse.setStatus(KakaoStatusType.CANCEL_PAYMENT);

        MockResponse response = new MockResponse()
                .setBody("{\"status\":\"CANCEL_PAYMENT\"}")
                .addHeader("Content-Type", "application/json");
        mockWebServer.enqueue(response);

        PaymentRefundDto.Response result = kakaoRefundService.refundPayment(request, null);

        assertNotNull(result);
        assertEquals(KakaoStatusType.CANCEL_PAYMENT, result.getStatus());
        verify(paymentRepository).findById(request.getPaymentId());
        verify(lessonService).updateParticipantCount(reservation.getLesson(), request.getQuantity());
    }

    @Test
    @DisplayName("환불 실패_유효하지 않은 결제 ID")
    void refundPayment_InvalidPaymentId() {
        when(paymentRepository.findById(request.getPaymentId())).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            kakaoRefundService.refundPayment(request, null);
        });

        assertEquals(ErrorCode.INVALID_PAYMENT_ID, exception.getErrorCode());
        verify(paymentRepository).findById(request.getPaymentId());
        verify(lessonService, never()).updateParticipantCount(lesson, 1);
    }

    @Test
    @DisplayName("환불 실패_유효하지 않은 예약 ID")
    void refundPayment_InvalidReservationId() {
        payment.setReservation(null);
        when(paymentRepository.findById(request.getPaymentId())).thenReturn(Optional.of(payment));

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            kakaoRefundService.refundPayment(request, null);
        });

        assertEquals(ErrorCode.INVALID_RESERVATION_ID, exception.getErrorCode());
        verify(paymentRepository).findById(request.getPaymentId());
        verify(lessonService, never()).updateParticipantCount(lesson, 1);
    }

    @Test
    @DisplayName("환불 실패")
    void refundPayment_NoRefundAvailable() {
        when(paymentRepository.findById(request.getPaymentId())).thenReturn(Optional.of(payment));

        MockResponse response = new MockResponse().setResponseCode(500);
        mockWebServer.enqueue(response);

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            kakaoRefundService.refundPayment(request, null);
        });

        assertEquals(ErrorCode.PAY_ERROR, exception.getErrorCode());
        verify(paymentRepository).findById(request.getPaymentId());
        verify(lessonService, never()).updateParticipantCount(lesson, 1);
    }

    @Test
    @DisplayName("환불 조회 성공")
    void getAllRefunds_success() {
        List<Refund> refunds = Arrays.asList(refund);

        when(refundRepository.findAll()).thenReturn(refunds);

        List<PaymentRefundDto> refundDtos = refundService.getAllRefunds();

        assertNotNull(refundDtos);
        assertEquals(1, refundDtos.size());
        assertEquals(refund.getRefundId(), refundDtos.get(0).getRefundId());
        verify(refundRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("특정 환불 조회 성공")
    void getRefundById_success() {
        Long refundId = 1L;

        when(refundRepository.findById(refundId)).thenReturn(Optional.of(refund));

        PaymentRefundDto refundDto = refundService.getRefundById(refundId);

        assertNotNull(refundDto);
        assertEquals(refundId, refundDto.getRefundId());
        verify(refundRepository, times(1)).findById(refundId);
    }

    @Test
    @DisplayName("환불 조회 실패")
    void getRefundById_notFound() {
        Long refundId = 1L;

        when(refundRepository.findById(refundId)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            refundService.getRefundById(refundId);
        });

        assertEquals(REFUND_NOT_FOUND, exception.getErrorCode());
        verify(refundRepository, times(1)).findById(refundId);
    }

}
