package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.LESSON_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.RESERVATION_NOT_FOUND;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;

import com.linked.classbridge.domain.Attendance;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Payment;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.payment.PaymentStatusType;
import com.linked.classbridge.dto.refund.PaymentRefundDto;
import com.linked.classbridge.dto.reservation.GetReservationResponse;
import com.linked.classbridge.dto.reservation.RegisterReservationDto;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.type.Gender;
import com.linked.classbridge.type.ReservationStatus;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.AttendanceRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.util.AgeUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final AttendanceRepository attendanceRepository;
    private final OneDayClassRepository oneDayClassRepository;
    private final UserService userService;
    private final KakaoRefundService refundService;

    /**
     * 예약 생성
     */
    @Transactional
    public Reservation createReservation(RegisterReservationDto.Request request) {

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new RestApiException(LESSON_NOT_FOUND));

        String userEmail = userService.getCurrentUserEmail();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        Reservation reservation = reservationRepository.save(Reservation.createReservation(request, lesson, user));

        attendanceRepository.save(Attendance.createAttendance(lesson, reservation, user)); // 출석 테이블에 유저 추가

        OneDayClass oneDayClass = lesson.getOneDayClass();
        Double userAge = (double) AgeUtil.calculateAge(user.getBirthDate());
        Gender userGender = user.getGender();

        oneDayClass.addStudent(userAge, userGender);
        oneDayClassRepository.save(oneDayClass);

        return reservation;
    }

    /**
     * 특정 예약 조회
     */
    @Transactional(readOnly = true)
    public GetReservationResponse getReservation(Long reservationId) {
        Reservation reservation = getReservationEntity(reservationId);

        return GetReservationResponse.from(reservation);
    }

    private Reservation getReservationEntity(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RestApiException(RESERVATION_NOT_FOUND));
    }

    /**
     * 특정 예약 강사 취소
     */
    @Transactional
    public Long cancelReservation(Long reservationId) {
        Reservation reservation = getReservationEntity(reservationId);
        // 환불 처리
        Payment payment = reservation.getPayment();
        if (payment != null) {
            PaymentRefundDto.Requset refundRequest = new PaymentRefundDto.Requset();
            refundRequest.setPaymentId(payment.getPaymentId());
            refundRequest.setRefundType(PaymentStatusType.REFUNDED_BY_TUTOR); // 전체 환불
            refundRequest.setCancelAmount(payment.getTotalAmount());
            refundRequest.setQuantity(reservation.getQuantity());

            // 환불 처리 메소드 호출
            refundService.refundPayment(refundRequest, null);
        }
        return reservation.getReservationId();
    }

    /**
     * 모든 예약 조회
     */
    @Transactional(readOnly = true)
    public List<GetReservationResponse> getReservations(Long userId, Long lessonId, String status) {

        ReservationStatus reservationStatus = status != null ? ReservationStatus.valueOf(status) : null;

        List<Reservation> reservations = reservationRepository.findReservations(userId, lessonId, reservationStatus);
        return reservations.stream()
                .map(GetReservationResponse::from)
                .collect(Collectors.toList());
    }
}
