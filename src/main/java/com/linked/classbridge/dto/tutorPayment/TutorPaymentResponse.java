package com.linked.classbridge.dto.tutorPayment;

import com.linked.classbridge.domain.TutorPayment;
import com.linked.classbridge.dto.tutorPayment.TutorPaymentDetailResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorPaymentResponse {
    private Long tutorPaymentId;
    private int amount;
    private LocalDateTime paymentDateTime;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private Long userId;
    private List<TutorPaymentDetailResponse> details;

    public TutorPaymentResponse(TutorPayment tutorPayment, List<TutorPaymentDetailResponse> details) {
        this.tutorPaymentId = tutorPayment.getTutorPaymentId();
        this.amount = tutorPayment.getAmount();
        this.paymentDateTime = tutorPayment.getPaymentDateTime();
        this.periodStartDate = tutorPayment.getPeriodStartDate();
        this.periodEndDate = tutorPayment.getPeriodEndDate();
        this.userId = tutorPayment.getUserId();
        this.details = details;
    }
}
