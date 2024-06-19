package com.linked.classbridge.dto.reservation;

import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.oneDayClass.LessonDto;
import com.linked.classbridge.dto.payment.GetPaymentResponse;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.type.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetReservationResponse {
    private Long reservationId;
    private UserDto user;
    private LessonDto lesson;
    private ReservationStatus status;
    private int quantity;
    private GetPaymentResponse payment;

    public static GetReservationResponse from(Reservation reservation) {
        return GetReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .user(UserDto.from(reservation.getUser()))
                .lesson(LessonDto.from(reservation.getLesson()))
                .status(reservation.getStatus())
                .quantity(reservation.getQuantity())
                .payment(GetPaymentResponse.from(reservation.getPayment()))
                .build();
    }
}
