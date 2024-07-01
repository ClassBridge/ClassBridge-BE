package com.linked.classbridge.domain;

import com.linked.classbridge.domain.BaseEntity;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.TutorPayment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TutorPaymentDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tutorPaymentDetailId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_payment_id", nullable = false)
    private TutorPayment tutorPayment;

}
