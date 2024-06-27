package com.linked.classbridge.dto.reservation;

import com.linked.classbridge.type.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuccessReservationDto {
    private Long reservationId;
    private ReservationStatus status;
    private int quantity;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String className;
    private int price;
    private int duration;
    private String address1;
    private String address2;
    private String address3;
    private String nickname;
    private int totalPrice;
}
