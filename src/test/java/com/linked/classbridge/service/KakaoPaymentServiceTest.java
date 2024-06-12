package com.linked.classbridge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.linked.classbridge.config.PayProperties;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.PaymentRepository;
import java.io.IOException;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class KakaoPaymentServiceTest {

    @Mock
    private PayProperties payProperties;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private KakaoPaymentService kakaoPaymentService;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        kakaoPaymentService = new KakaoPaymentService(payProperties, WebClient.builder(), paymentRepository);
    }

    @AfterEach
    void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testInitiatePayment_Success() {
        // given
        PaymentPrepareDto.Request request = new PaymentPrepareDto.Request();
        request.setItemName("Test Item");
        request.setQuantity(1);
        request.setTotalAmount(1000);
        request.setTexFreeAmount(0);

        Map<String, String> parameters = Map.of(
                "cid", "test_cid",
                "partner_order_id", "payservice1",
                "partner_user_id", "payservice1",
                "item_name", "Test Item",
                "quantity", "1",
                "total_amount", "1000",
                "tax_free_amount", "0",
                "approval_url", "http://localhost:8080/api/payments/complete",
                "cancel_url", "http://localhost:8080/api/payments/cancel",
                "fail_url", "http://localhost:8080/api/payments/fail"
        );

        PaymentPrepareDto.Response response = new PaymentPrepareDto.Response();
        response.setTid("test_tid");

        given(payProperties.getCid()).willReturn("test_cid");
        given(payProperties.getReadyUrl()).willReturn(mockWebServer.url("/api/payments/prepare").toString());

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"tid\":\"test_tid\"}")
                .addHeader("Content-Type", "application/json"));

        // when
        PaymentPrepareDto.Response result = kakaoPaymentService.initiatePayment(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTid()).isEqualTo("test_tid");
    }

    @Test
    void testInitiatePayment_Failure() {
        // given
        PaymentPrepareDto.Request request = new PaymentPrepareDto.Request();

        given(payProperties.getReadyUrl()).willReturn(mockWebServer.url("/api/payments/prepare").toString());

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
}