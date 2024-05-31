package com.linked.classbridge.controller;

import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Response;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.PaymentService;
import com.linked.classbridge.type.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 요청")
    @PostMapping("/prepare")
    public String initiatePayment(@RequestBody Request paymentRequest, Authentication authentication,
                                  HttpServletRequest request) {
        Response initiatedPayment = paymentService.initiatePayment(paymentRequest, authentication);
        String token = UUID.randomUUID().toString();
        HttpSession session = request.getSession();
        session.setAttribute("paymentToken", token);
        session.setAttribute("initiatedPaymentResponse", initiatedPayment);
        log.info("Session attribute 'initiatedPaymentResponse' set: {}, Token: {}", initiatedPayment, token);


        // 세션에 저장
//        request.getSession().setAttribute("initiatedPaymentResponse", initiatedPayment);
//        log.info("Session attribute 'initiatedPaymentResponse' set: {}", initiatedPayment);
//        log.info("Session attribute 'initiatedPaymentResponse' set: {}, Session ID: {}", initiatedPayment, session.getId());
        String redirectUrl = initiatedPayment.getNext_redirect_pc_url() + "?token=" + token;
        return redirectUrl;

//        return initiatedPayment.getNext_redirect_pc_url();
    }

    /**
     * 결제 성공
     */
    @GetMapping("/complete")
    public String approvePayment(HttpServletRequest request, Authentication authentication,
                                                     @RequestParam("pg_token") String pgToken,
                                 @RequestParam("token") String token) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.error("Session not found or expired.");
            throw new RestApiException(ErrorCode.MISSING_PAY_RESPONSE_IN_SESSION);
        }
        String sessionToken = (String) session.getAttribute("paymentToken");
        if (sessionToken == null || !sessionToken.equals(token)) {
            log.error("Invalid or missing token.");
            throw new RestApiException(ErrorCode.INVALID_TOKEN);
        }
//        PaymentPrepareDto.Response paymentResponse = (Response) request.getSession().getAttribute("initiatedPaymentResponse");
        PaymentPrepareDto.Response paymentResponse = (Response) session.getAttribute("initiatedPaymentResponse");
        if (paymentResponse == null) {
            log.error("Session attribute 'initiatedPaymentResponse' not found");

            throw new RestApiException(ErrorCode.MISSING_PAY_RESPONSE_IN_SESSION);
        }
        paymentResponse.setPgToken(pgToken);

        log.info("success :: {}", pgToken);
        return paymentService.approveResponse(paymentResponse, authentication, request.getHeader("Authorization"), token);
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
