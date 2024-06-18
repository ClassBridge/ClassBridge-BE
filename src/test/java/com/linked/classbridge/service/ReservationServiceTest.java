package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.RESERVATION_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.reservation.GetReservationResponse;
import com.linked.classbridge.dto.reservation.RegisterReservationDto;
import com.linked.classbridge.dto.reservation.ReservationStatus;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private RegisterReservationDto.Request request;
    private Lesson lesson;
    private User user;
    private Payment payment;
    private Reservation reservation;


    @BeforeEach
    void setUp() {
        request = new RegisterReservationDto.Request();
        request.setLessonId(1L);
        request.setUserId(1L);

        lesson = new Lesson();
        lesson.setLessonId(1L);

        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        payment = new Payment();
        payment.setPaymentId(1L);

        reservation = Reservation.createReservation(request, lesson, user);
        reservation.setReservationId(1L);
//        reservation.setUser(user);
//        reservation.setLesson(lesson);
        reservation.setPayment(payment);
//        reservation.setStatus(ReservationStatus.PENDING);

    }

    @Test
    @DisplayName("예약 성공")
    void createReservation_Success() {
        when(lessonRepository.findById(request.getLessonId())).thenReturn(Optional.of(lesson));
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.of(user));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation createdReservation = reservationService.createReservation(request);

        assertNotNull(createdReservation);
        verify(lessonRepository).findById(request.getLessonId());
        verify(userRepository).findById(request.getUserId());
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("예약 실패_수업 정보 없음")
    void createReservation_LessonNotFound() {
        when(lessonRepository.findById(request.getLessonId())).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            reservationService.createReservation(request);
        });

        assertEquals(ErrorCode.LESSON_NOT_FOUND, exception.getErrorCode());
        verify(lessonRepository).findById(request.getLessonId());
        verify(userRepository, never()).findById(1L);
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    @DisplayName("예약 실패_회원 정보 없음")
    void createReservation_UserNotFound() {
        when(lessonRepository.findById(request.getLessonId())).thenReturn(Optional.of(lesson));
        when(userRepository.findById(request.getUserId())).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            reservationService.createReservation(request);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(lessonRepository).findById(request.getLessonId());
        verify(userRepository).findById(request.getUserId());
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    @DisplayName("예약 조회 성공")
    void getReservation_success() {
        Long reservationId = 1L;

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        GetReservationResponse response = reservationService.getReservation(reservationId);

        assertNotNull(response);
        assertEquals(reservationId, response.getReservationId());
    }

    @Test
    @DisplayName("예약 조회 실패")
    void getReservation_notFound() {
        Long reservationId = 1L;

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(RestApiException.class, () -> {
            reservationService.getReservation(reservationId);
        });

        assertEquals(RESERVATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservation_success() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation();
        reservation.setReservationId(reservationId);
        reservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Long canceledReservationId = reservationService.cancelReservation(reservationId);

        assertEquals(reservationId, canceledReservationId);
        assertEquals(ReservationStatus.CANCELED_BY_TUTOR, reservation.getStatus());
        verify(reservationRepository, times(1)).save(reservation);
    }

}
