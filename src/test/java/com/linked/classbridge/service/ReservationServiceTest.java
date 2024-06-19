package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.RESERVATION_NOT_FOUND;
import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linked.classbridge.domain.Attendance;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.reservation.GetReservationResponse;
import com.linked.classbridge.dto.reservation.RegisterReservationDto;
import com.linked.classbridge.repository.AttendanceRepository;
import com.linked.classbridge.type.ReservationStatus;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

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

    @Mock
    private UserService userService;

    @Mock
    private AttendanceRepository attendanceRepository;

    private RegisterReservationDto.Request request;
    private Lesson lesson;
    private User user;
    private Payment payment;
    private Reservation reservation;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        request = new RegisterReservationDto.Request();
        request.setLessonId(1L);
        request.setQuantity(1);

        lesson = new Lesson();
        lesson.setLessonId(1L);

        user = new User();
        user.setEmail("test@example.com");

        payment = new Payment();
        payment.setPaymentId(1L);

        reservation = Reservation.builder()
                .lesson(lesson)
                .user(user)
                .payment(payment)
                .quantity(request.getQuantity())
                .status(ReservationStatus.PENDING)
                .build();

        attendance = Attendance.builder()
                .lesson(lesson)
                .reservation(reservation)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("예약 성공")
    void createReservation_Success() {

        // given
        given(lessonRepository.findById(request.getLessonId())).willReturn(Optional.of(lesson));
        given(userService.getCurrentUserEmail()).willReturn(user.getEmail());
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

        // when
        reservationService.createReservation(request);

        // then
        verify(lessonRepository).findById(request.getLessonId());
        verify(userService).getCurrentUserEmail();
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("예약 실패_수업 정보 없음")
    void createReservation_LessonNotFound() {
        // given
        given(lessonRepository.findById(request.getLessonId())).willReturn(Optional.empty());

        // when & then
        assertThrows(RestApiException.class, () -> reservationService.createReservation(request));
        verify(lessonRepository).findById(request.getLessonId());

    }

    @Test
    @DisplayName("예약 실패_회원 정보 없음")
    void createReservation_UserNotFound() {
        // given
        given(lessonRepository.findById(request.getLessonId())).willReturn(Optional.of(lesson));
        given(userService.getCurrentUserEmail()).willReturn(user.getEmail());
        given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThrows(RestApiException.class, () -> reservationService.createReservation(request));
        verify(lessonRepository).findById(request.getLessonId());
        verify(userService).getCurrentUserEmail();
        verify(userRepository).findByEmail(user.getEmail());

    }

    @Test
    @DisplayName("예약 조회 성공")
    void getReservation_success() {
        Long reservationId = 1L;

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        GetReservationResponse response = reservationService.getReservation(reservationId);

        assertNotNull(response);
        assertEquals(lesson.getLessonId(), response.getLesson().getLessonId());
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
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

}
