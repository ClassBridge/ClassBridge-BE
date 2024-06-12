package com.linked.classbridge.domain;

import com.linked.classbridge.dto.payment.KakaoStatusType;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
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
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;
    @OneToOne()
    private Payment payment;
    private int amount;                 // 최초 실 결제 금액
    private int approvedCancelAmount;   // 이번 요청으로 취소된 전체 결제 금액
    private int canceledAmount;         // 취소된 전체 누적 금액
    private int cancelAvailableAmount;  // 전체 취소 가능 금액
    private int quantity;               // 환불 수량
    @Enumerated(EnumType.STRING)
    private PaymentStatusType status;
    private LocalDateTime approved_at;
    private LocalDateTime canceled_at;
}
