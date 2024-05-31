package com.linked.classbridge.service;

import com.linked.classbridge.dto.payment.PaymentApproveDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto;
import com.linked.classbridge.dto.payment.PaymentPrepareDto.Request;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;

public interface PaymentProcessor {
    PaymentPrepareDto.Response initiatePayment(Request request);
//    String approvePayment(PaymentPrepareDto.Response response, Authentication authentication,
//                                              String header);

    String approvePayment(PaymentPrepareDto.Response response, Authentication authentication,
                          String header, String token);
}
