package com.linked.classbridge.dto.reservation;

import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ReservationDto {
    @Getter
    public static class Request {
        private Long userId;
        private Long lessonId;
        private int quantity;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private Long reservationId;
        private PaymentStatusType status;
        private LocalDateTime createdAt;
    }

    public static ReservationDto.Response convertToDto(Reservation reservation) {
        return ReservationDto.Response.builder()
                .reservationId(reservation.getReservationId())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
