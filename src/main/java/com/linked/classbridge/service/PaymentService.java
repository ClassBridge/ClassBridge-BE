package com.linked.classbridge.service;

import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentProcessor paymentProcessor;

    /**
     * 결제 요청
     */
    @Transactional
    public PaymentPrepareDto.Response initiatePayment(Request request, Authentication authentication) {
        return paymentProcessor.initiatePayment(request);
    }

    /**
     * 결제 완료 승인
     */
    public String approveResponse(PaymentPrepareDto.Response response, Authentication authentication,
                                             String header, String token) {
        return paymentProcessor.approvePayment(response, authentication, header, token);
    }


}
