package com.linked.classbridge.service;

import com.linked.classbridge.config.PayProperties;
import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Response;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.type.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * 결제 요청 생성
 * 결제 승인
 * 결제 취소
 * 결제 상태 조회
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoPaymentService {

    private final PayProperties payProperties;
    private final WebClient.Builder webClient;
    private PaymentPrepareDto.Response paymentResponse;

    /**
     * 카카오페이 결제 요청 로직
     */
//    @Override
    @Transactional
    public PaymentPrepareDto.Response initiatePayment(Request request) {

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

    public ResponseEntity<String> approvePayment(PaymentPrepareDto.Response response, String header) {
        try {
            // 카카오 요청
            Map<String, String> parameters = new HashMap<>();
            parameters.put("cid", payProperties.getCid());
            parameters.put("tid", response.getTid());
            parameters.put("partner_order_id", response.getPartnerOrderId());
            parameters.put("partner_user_id", response.getPartnerUserId());
            parameters.put("pg_token", response.getPgToken());

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

    private Map<String, String> getInitiateParameters(Request request) {

        request.setPartnerUserId("payservice1");
        request.setPartnerOrderId("payservice1");

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
