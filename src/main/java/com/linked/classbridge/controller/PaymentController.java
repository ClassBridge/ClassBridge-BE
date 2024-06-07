package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.payment.CreatePaymentResponse;
import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import com.linked.classbridge.service.KakaoPaymentService;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.security.core.Authentication;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final KakaoPaymentService paymentService;

    private PaymentPrepareDto.Response paymentResponse;

    @Operation(summary = "결제 요청")
    @PostMapping("/prepare")
    public String initiatePayment(@RequestBody Request paymentRequest) {
        paymentResponse = paymentService.initiatePayment(paymentRequest);
        paymentResponse.setPartnerOrderId(paymentRequest.getPartnerOrderId());
        paymentResponse.setPartnerUserId(paymentRequest.getPartnerUserId());

        return paymentResponse.getNext_redirect_pc_url();
    }

    /**
     * 결제 성공
     */
    @GetMapping("/complete")
    public ResponseEntity<String> approvePayment(HttpServletRequest request,
                                                 @RequestParam("pg_token") String pgToken) throws Exception {
        paymentResponse.setCid("TC0ONETIME");
        paymentResponse.setPgToken(pgToken);

        return paymentService.approvePayment(paymentResponse, request.getHeader("Authorization"));
    }

    @PostMapping("/complete")
    public ResponseEntity<SuccessResponse<CreatePaymentResponse>> completePayment(
            @RequestBody PaymentApproveDto.Response paymentResponse) {

            // 결제 승인 응답 데이터 처리
            return ResponseEntity.status(HttpStatus.OK).body(
                    SuccessResponse.of(
                            ResponseMessage.PAYMENT_SUCCESS,
                            paymentService.savePayment(paymentResponse)
                    )
            );
    }


//    @PostMapping("/approve")
//    public ResponseEntity<PaymentResponse> approvePayment(@RequestParam String transactionId) {
//        PaymentResponse response = paymentService.approvePayment(transactionId);
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/cancel")
//    public ResponseEntity<Void> cancelPayment(@RequestParam String transactionId) {
//        paymentService.cancelPayment(transactionId);
//        return ResponseEntity.noContent().build();
//    }
}
