package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Payment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.reservation.lesson.lessonDate < :currentDate AND p.status = 'COMPLETED'")
    List<Payment> findAllByPaymentDateBetweenAndLessonDateBeforeAndStatusCompleted(LocalDateTime startDate, LocalDateTime endDate, LocalDate currentDate);

//    List<Payment> findByUserIdAndUpdatedAtBetween(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT p FROM Payment p JOIN p.reservation r WHERE r.user.userId = :userId AND p.updatedAt BETWEEN :startDateTime AND :endDateTime")
    List<Payment> findByUserIdAndPaymentDateTimeBetween(@Param("userId") Long userId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT p FROM Payment p WHERE p.reservation.user.userId = :userId")
    List<Payment> findAllByUserId(@Param("userId") Long userId);
}
