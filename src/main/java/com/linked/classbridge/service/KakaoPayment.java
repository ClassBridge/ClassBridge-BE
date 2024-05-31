package com.linked.classbridge.service;

import com.linked.classbridge.config.PayProperties;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Response;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.type.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * 결제 요청 생성
 * 결제 승인
 * 결제 취소
 * 결제 상태 조회
 */
@RequiredArgsConstructor
@Component
public class KakaoPayment implements PaymentProcessor {

    private final PayProperties payProperties;
    private final WebClient.Builder webClient;

    /**
     * 카카오페이 결제 요청 로직
     */
    @Override
    public PaymentPrepareDto.Response initiatePayment(Request request) {

        request.setPartnerUserId("dummyData");
        request.setPartnerOrderId("dummyData");

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

    @Override
    public String approvePayment(PaymentPrepareDto.Response response, Authentication authentication,
                                 String header, String token) {
        try {
            // 카카오 요청
            Map<String, String> parameters = new HashMap<>();
            parameters.put("cid", payProperties.getCid());
            parameters.put("tid", response.getTid());
            parameters.put("partner_order_id", response.getPartnerOrderId());
            parameters.put("partner_user_id", response.getPartnerUserId());
            parameters.put("pg_token", response.getPgToken());

            // WebClient로 외부에 요청
            webClient.build().post()
//                    .uri(payProperties.getApproveUrl())
                    .uri(uriBuilder -> uriBuilder
                            .path(payProperties.getApproveUrl())
                            .queryParam("token", token) // Include token in the URL
                            .build())
                    .headers(httpHeaders -> {
                        httpHeaders.setAll(getHeaders().toSingleValueMap());
                    })
                    .bodyValue(parameters)
                    .retrieve()
                    .bodyToMono(Response.class)
                    .block(); // 동기식 처리

            // 새로운 요청 생성
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", header);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentPrepareDto.Response> newRequestEntity = new HttpEntity<>(response, headers);

            // WebClient로 POST 요청 보내기
            String newResponse = webClient.build().post()
                    .uri("http://localhost:8080/api/payments/complete")
                    .headers(httpHeaders -> {
                        httpHeaders.setAll(headers.toSingleValueMap());
                    })
                    .body(Mono.just(newRequestEntity), PaymentPrepareDto.Response.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 동기식 처리

            return newResponse;

        } catch (WebClientResponseException e) {
            throw new RestApiException(ErrorCode.PAY_ERROR);
        }
    }

    private Map<String, String> getInitiateParameters(Request request) {

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

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String auth = "SECRET_KEY " + payProperties.getDevKey();

        httpHeaders.set("Authorization", auth);
        httpHeaders.set("Content-type", "application/json");

        return httpHeaders;
    }
}
