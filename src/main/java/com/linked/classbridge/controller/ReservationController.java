package com.linked.classbridge.controller;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.reservation.GetReservationResponse;
import com.linked.classbridge.dto.reservation.RegisterReservationDto;
import com.linked.classbridge.dto.reservation.RegisterReservationDto.Response;
import com.linked.classbridge.service.ReservationService;
import com.linked.classbridge.type.ResponseMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 생성
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<Response>> createReservation(
            @RequestBody RegisterReservationDto.Request request) {
        log.info("auth user email :: {}", SecurityContextHolder.getContext().getAuthentication().getName());

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.RESERVATION_REGISTER_SUCCESS,
                        RegisterReservationDto.convertToDto(reservationService.createReservation(request))
                )
        );
    }

    /**
     * 특정 예약 조회
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<SuccessResponse<GetReservationResponse>> getReservation(@PathVariable Long reservationId) {
        GetReservationResponse reservation = reservationService.getReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.RESERVATION_GET_SUCCESS,
                        reservation
                )
        );
    }

    /**
     * 특정 예약 강사 취소
     */
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<SuccessResponse<Long>> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.RESERVATION_CANCELED_BY_TUTOR_SUCCESS,
                        reservationService.cancelReservation(reservationId)
                )
        );
    }

    /**
     * 모든 예약 조회
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<List<GetReservationResponse>>> getReservations(
            @RequestParam(required = false, name = "user_id") Long userId,
            @RequestParam(required = false, name = "lesson_id") Long lessonId,
            @RequestParam(required = false) String status) {
        List<GetReservationResponse> reservations = reservationService.getReservations(userId, lessonId, status);
        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.RESERVATION_GET_SUCCESS,
                        reservations
                )
        );
    }

}
