package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Lesson;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Modifying
    @Query(value = "UPDATE Lesson SET deletedAt = now() WHERE oneDayClass.classId = :classId and lessonDate >= :lessonDate")
    void deleteAllByOneDayClassClassIdAndLessonDateIsAfter(@Param("classId") Long classId, @Param("lessonDate") LocalDate lessonDate);

    boolean existsByOneDayClassClassIdAndLessonDateIsAfterAndParticipantNumberIsGreaterThan(Long classId, LocalDate endDate, int zero);


    void deleteAllByOneDayClassClassIdAndLessonDateIsBefore(Long classId, LocalDate startDate);

    boolean existsByOneDayClassClassIdAndLessonDateIsBetweenAndParticipantNumberIsGreaterThan(long classId, LocalDate startDate, LocalDate changeStartDate,
                                                                                              int zero);
    List<Lesson> findAllByOneDayClassClassIdAndLessonDateIsAfter(long classId, LocalDate localDate);

    boolean existsByOneDayClassClassIdAndParticipantNumberIsGreaterThanAndLessonDateIsAfter(long classId, int personal, LocalDate now);

    boolean existsByOneDayClassClassIdAndLessonDateAndStartTime(Long classId, LocalDate localDate, LocalTime localTime);

    boolean existsByOneDayClassClassIdAndLessonDateIsAfterAndParticipantNumberIsLessThan(Long classId, LocalDate localDate, int personal);

    List<Lesson> findAllByOneDayClassClassIdAndLessonDateIsAfterOrderByLessonDateAscStartTimeAsc(long classId, LocalDate localDate);

    List<Lesson> findAllByOneDayClassClassIdOrderByLessonDateAscStartTimeAsc(long classId);
}
