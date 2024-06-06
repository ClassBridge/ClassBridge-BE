package com.linked.classbridge.repository;

import com.linked.classbridge.domain.Lesson;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    void deleteAllByOneDayClassClassIdAndLessonDateIsAfter(Long classId, LocalDate lessonDate);

    boolean existsByOneDayClassClassIdAndLessonDateIsAfterAndParticipantNumberIsGreaterThan(Long classId, LocalDate endDate, int zero);


    void deleteAllByOneDayClassClassIdAndLessonDateIsBefore(Long classId, LocalDate startDate);

    boolean existsByOneDayClassClassIdAndLessonDateIsBetweenAndParticipantNumberIsGreaterThan(long classId, LocalDate startDate, LocalDate changeStartDate,
                                                                                              int zero);

    List<Lesson> findAllByOneDayClassClassIdAndLessonDateIsAfter(long classId, LocalDate localDate);

    boolean existsByOneDayClassClassIdAndParticipantNumberIsGreaterThanAndLessonDateIsAfter(long classId, int personal, LocalDate now);
}
