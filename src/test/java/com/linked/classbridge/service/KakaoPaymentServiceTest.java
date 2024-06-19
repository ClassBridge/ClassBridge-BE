package com.linked.classbridge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.config.PayProperties;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.payment.GetPaymentResponse;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.PaymentRepository;
import com.linked.classbridge.repository.ReservationRepository;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class KakaoPaymentServiceTest {

    @Mock
    private PayProperties payProperties;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private KakaoPaymentService kakaoPaymentService;

    private MockWebServer mockWebServer;

    private Payment payment;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        kakaoPaymentService = new KakaoPaymentService(payProperties, WebClient.builder(), paymentRepository,
                reservationRepository, lessonRepository, lessonService);

        payment = new Payment();
        payment.setPaymentId(1L);
        payment.setCid("testCid");
        payment.setPartnerOrderId("testOrderId");
        payment.setPartnerUserId("testUserId");
        payment.setItemName("testItem");
        payment.setQuantity(1);
        payment.setTotalAmount(1000);
        payment.setPaymentMethodType("CARD");
        payment.setTid("testTid");
        payment.setStatus(PaymentStatusType.COMPLETED);
    }

    @AfterEach
    void shutdown() throws IOException {
        mockWebServer.shutdown();
    }
    @Test
    void testInitiatePayment_MaxParticipantsExceeded() {
        // given
        PaymentPrepareDto.Request request = new PaymentPrepareDto.Request();
        request.setReservationId(1L);
        request.setQuantity(5);
        request.setItemName("Test Item");
        request.setTotalAmount(1000);
        request.setTexFreeAmount(0);

        Lesson lesson = new Lesson();
        lesson.setLessonId(1L);

        Reservation reservation = new Reservation();
        reservation.setLesson(lesson);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // when & then
        assertThrows(RestApiException.class, () -> kakaoPaymentService.initiatePayment(request));
    }

    @Test
    void testInitiatePayment_Failure() {
        // given
        PaymentPrepareDto.Request request = new PaymentPrepareDto.Request();
        request.setReservationId(1L);
        request.setQuantity(1);
        request.setItemName("Test Item");
        request.setTotalAmount(1000);
        request.setTexFreeAmount(0);

        Lesson lesson = new Lesson();
        lesson.setLessonId(1L);

        Reservation reservation = new Reservation();
        reservation.setLesson(lesson);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        // when & then
        assertThrows(RestApiException.class, () -> kakaoPaymentService.initiatePayment(request));
    }

    @Test
    void testApprovePayment_Failure() {
        // given
        PaymentPrepareDto.Response paymentResponse = new PaymentPrepareDto.Response();

        given(payProperties.getApproveUrl()).willReturn(mockWebServer.url("/approve").toString());

        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        // when & then
        assertThrows(RestApiException.class, () -> kakaoPaymentService.approvePayment(paymentResponse, "header"));
    }

    @Test
    void getAllPayments_success() {
        List<Payment> payments = Arrays.asList(payment);

        when(paymentRepository.findAll()).thenReturn(payments);

        List<GetPaymentResponse> paymentDtos = kakaoPaymentService.getAllPayments();

        assertNotNull(paymentDtos);
        assertEquals(1, paymentDtos.size());
        assertEquals(payment.getPaymentId(), paymentDtos.get(0).getPaymentId());
        verify(paymentRepository, times(1)).findAll();
    }
}