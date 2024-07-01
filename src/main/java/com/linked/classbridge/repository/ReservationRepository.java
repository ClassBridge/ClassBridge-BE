package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Reservation;
import com.linked.classbridge.dto.reservation.SuccessReservationDto;
import com.linked.classbridge.type.ReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByLesson(Lesson lesson);
    @Query("SELECT r FROM Reservation r WHERE " +
            "(:userId IS NULL OR r.user.userId = :userId) AND " +
            "(:lessonId IS NULL OR r.lesson.lessonId = :lessonId) AND " +
            "(:status IS NULL OR r.status = :status) ")
    List<Reservation> findReservations(
            @Param("userId") Long userId,
            @Param("lessonId") Long lessonId,
            @Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.lesson WHERE r.reservationId = :reservationId")
    Optional<Reservation> findByIdWithLesson(@Param("reservationId") Long reservationId);

    @Query("SELECT new com.linked.classbridge.dto.reservation.SuccessReservationDto( "
            + " r.reservationId, r.status, r.quantity, "
            + " l.lessonDate, l.startTime, l.endTime, "
            + " o.className, o.price, o.duration, o.address1, o.address2, o.address3, "
            + " t.nickname, o.price * r.quantity) "
            + " FROM Reservation r JOIN r.lesson l JOIN l.oneDayClass o JOIN o.tutor t "
            + " WHERE r.reservationId = :reservationId")
    SuccessReservationDto findByIdAndGetOneDayClass(Long reservationId);

    @Query("SELECT o FROM Reservation r JOIN r.lesson l JOIN l.oneDayClass o WHERE r.reservationId = :reservationId")
    Optional<OneDayClass> findOneDayClassById(Long reservationId);
}
