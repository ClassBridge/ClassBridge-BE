package com.linked.classbridge.dto.payment;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.dto.oneDayClass.LessonDto;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GetPaymentResponse {
    private Long paymentId;
    private String itemName;
    private int quantity;
    private int totalAmount;
    private PaymentStatusType status;
    public static GetPaymentResponse from(Payment payment) {
        return GetPaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .itemName(payment.getItemName())
                .quantity(payment.getQuantity())
                .totalAmount(payment.getTotalAmount())
                .status(payment.getStatus())
                .build();
    }
}
