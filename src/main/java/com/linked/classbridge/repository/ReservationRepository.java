package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.Reservation;
import java.util.List;
import com.linked.classbridge.type.ReservationStatus;
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
}
