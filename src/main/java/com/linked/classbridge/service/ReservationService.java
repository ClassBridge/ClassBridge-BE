package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.LESSON_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import com.linked.classbridge.dto.reservation.ReservationDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public Reservation createReservation(ReservationDto.Request request) {

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RestApiException(LESSON_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        return reservationRepository.save(Reservation.createReservation(request, lesson, user));
    }

}
