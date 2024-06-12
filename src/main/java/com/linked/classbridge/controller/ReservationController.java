package com.linked.classbridge.controller;

import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.reservation.ReservationDto;
import com.linked.classbridge.dto.reservation.ReservationDto.Response;
import com.linked.classbridge.service.ReservationService;
import com.linked.classbridge.type.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<SuccessResponse<Response>> createReservation(@RequestBody ReservationDto.Request request) {
        log.info("auth user email :: {}", SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.RESERVATION_SUCCESS,
                        ReservationDto.convertToDto(reservationService.createReservation(request))
                )
        );
    }
}
