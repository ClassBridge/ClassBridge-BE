package com.linked.classbridge.domain;
import com.linked.classbridge.domain.BaseEntity;
import com.linked.classbridge.domain.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TutorPayment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tutorPaymentId;

    private int amount;                         // 지급 금액

    @Column(nullable = false)
    private LocalDateTime paymentDateTime;      // 정산 일시

    @Column(nullable = false)
    private LocalDate periodStartDate;          // 지급 대상 기간 시작일

    @Column(nullable = false)
    private LocalDate periodEndDate;            // 지급 대상 기간 종료일

    @Column(nullable = false)
    private Long userId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User tutor;
}
