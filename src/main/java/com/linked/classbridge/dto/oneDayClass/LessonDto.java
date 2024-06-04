package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.Lesson;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LessonDto {
    private Long lessonId;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int personnel;
    private int participantNumber;

    public LessonDto(Lesson lesson) {
        lessonId = lesson.getLessonId();
        lessonDate = lesson.getLessonDate();
        startTime = lesson.getStartTime();
        endTime = lesson.getEndTime();
        personnel = lesson.getPersonnel();
        participantNumber = lesson.getParticipantNumber();
    }
}
