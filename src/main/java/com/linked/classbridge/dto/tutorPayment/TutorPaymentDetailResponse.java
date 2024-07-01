package com.linked.classbridge.dto.tutorPayment;

import com.linked.classbridge.domain.TutorPaymentDetail;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorPaymentDetailResponse {
    private Long tutorPaymentDetailId;
    private Long paymentId;
    private int totalAmount;
    private String itemName;
    private LocalDateTime paymentAt;

    public TutorPaymentDetailResponse(TutorPaymentDetail tutorPaymentDetail) {
        this.tutorPaymentDetailId = tutorPaymentDetail.getTutorPaymentDetailId();
        this.paymentId = tutorPaymentDetail.getPayment().getPaymentId();
        this.totalAmount = tutorPaymentDetail.getPayment().getTotalAmount();
        this.itemName = tutorPaymentDetail.getPayment().getItemName();
        this.paymentAt = tutorPaymentDetail.getPayment().getUpdatedAt();
    }
}
