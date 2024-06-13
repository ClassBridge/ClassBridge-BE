package com.linked.classbridge.dto.reservation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.linked.classbridge.domain.Reservation;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ReservationDto {
    @Getter
    @Setter
    public static class Request {
        @JsonProperty("user_id")
        private Long userId;
        @JsonProperty("lesson_id")
        private Long lessonId;
        private int quantity;
    }

    @Getter
    @Setter
    @Builder
    public static class Response {
        private Long reservationId;
        private ReservationStatus status;
        private LocalDateTime createdAt;
    }

    public static ReservationDto.Response convertToDto(Reservation reservation) {
        return Response.builder()
                .reservationId(reservation.getReservationId())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
