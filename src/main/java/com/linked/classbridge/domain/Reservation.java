package com.linked.classbridge.domain;

import com.linked.classbridge.dto.payment.PaymentStatusType;
import com.linked.classbridge.dto.reservation.ReservationDto;
import com.linked.classbridge.dto.reservation.ReservationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @OneToOne(fetch = FetchType.LAZY)
    private Lesson lesson;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private PaymentStatusType status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id") // 이 부분을 추가합니다.
    private Payment payment;

    public static Reservation createReservation(ReservationDto.Request request, Lesson lesson, User user) {
        return Reservation.builder()
                .lesson(lesson)
                .user(user)
                .quantity(request.getQuantity())
                .status(PaymentStatusType.PENDING)
                .build();
    }
}
