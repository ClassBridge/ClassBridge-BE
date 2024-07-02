package com.linked.classbridge.service;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.tutor.TutorInfoDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.AttendanceRepository;
import com.linked.classbridge.repository.ReservationRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.ReservationStatus;
import com.linked.classbridge.type.UserRole;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TutorService {

    private final BadgeService badgeService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final AttendanceRepository attendanceRepository;

    public TutorService(BadgeService badgeService, UserService userService, UserRepository userRepository,
                        ReservationRepository reservationRepository, AttendanceRepository attendanceRepository) {

        this.badgeService = badgeService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public String registerTutor(TutorInfoDto tutorInfoDto) {

        log.info("registering tutor");

        User user = userRepository.findByEmail(userService.getCurrentUserEmail())
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getRoles().contains(UserRole.ROLE_TUTOR)) {
            throw new RestApiException(ErrorCode.ALREADY_REGISTERED_TUTOR);
        }

        if (tutorInfoDto.getBank() == null || tutorInfoDto.getAccount() == null) {
            throw new RestApiException(ErrorCode.MISSING_BANK_ACCOUNT_INFO);
        }

        user.getRoles().add(UserRole.ROLE_TUTOR);
        user.setBankName(tutorInfoDto.getBank());
        user.setAccountNumber(tutorInfoDto.getAccount());
        user.setBusinessRegistrationNumber(tutorInfoDto.getBusinessRegistrationNumber());
        user.setSelfIntroduction(tutorInfoDto.getIntroduction());
        userRepository.save(user);

        log.info("tutor registered successfully");

        return "tutor registered successfully";
    }

    public String updateTutorInfo(TutorInfoDto tutorInfoDto){

        log.info("updating tutor info");

        User user = userRepository.findByEmail(userService.getCurrentUserEmail())
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        if(!user.getRoles().contains(UserRole.ROLE_TUTOR)){
            throw new RestApiException(ErrorCode.NOT_REGISTERED_TUTOR);
        }

        user.setBankName(tutorInfoDto.getBank() != null ? tutorInfoDto.getBank() : user.getBankName());
        user.setAccountNumber(tutorInfoDto.getAccount() != null ? tutorInfoDto.getAccount() : user.getAccountNumber());
        user.setBusinessRegistrationNumber(tutorInfoDto.getBusinessRegistrationNumber() != null ?
                tutorInfoDto.getBusinessRegistrationNumber() : user.getBusinessRegistrationNumber());
        user.setSelfIntroduction(tutorInfoDto.getIntroduction() != null ? tutorInfoDto.getIntroduction() : user.getSelfIntroduction());
        userRepository.save(user);

        log.info("tutor info updated successfully");

        return "tutor info updated successfully";
    }

    public String checkAttendance (Long userId, Long reservationId) {

        log.info("checking attendance");

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RestApiException(ErrorCode.RESERVATION_NOT_FOUND));

        LocalDate lessonDate = reservation.getLesson().getLessonDate();
        LocalTime lessonStartTime = reservation.getLesson().getStartTime();

        // 레슨 당일에만 출석체크 가능
        if(!lessonDate.isEqual(LocalDate.now())) {
            throw new RestApiException(ErrorCode.NOT_TODAY_LESSON);
        }

        // 레슨 시작 30분 전부터 출석체크 가능
        if(LocalTime.now().isBefore(lessonStartTime.minusMinutes(30))) {
            throw new RestApiException(ErrorCode.NOT_YET_ATTENDANCE);
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RestApiException(ErrorCode.USER_NOT_FOUND));

        attendanceRepository.findByReservationAndUser(reservation, user)
                .ifPresent(attendance -> {
                    attendance.setAttended(true);
                    attendanceRepository.save(attendance);
                });

        log.info("{}: attendance checked successfully", user.getEmail());

        String userEmail = user.getEmail();
        String categoryName = String.valueOf(reservation.getLesson().getOneDayClass().getCategory().getName());

        // 스탬프 부여
        badgeService.addStamp(userEmail, categoryName);

        log.info("{}: stamp issued successfully", user.getEmail());

        // 출석 확인 후 예약 상태 변경
        reservation.setStatus(ReservationStatus.ATTENDED);

        return "attendance checked and stamp issued successfully";
    }
}
