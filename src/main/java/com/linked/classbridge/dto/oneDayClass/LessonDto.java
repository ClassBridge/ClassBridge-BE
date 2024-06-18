package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
    private int participantNumber;
    private int personal;

    public LessonDto(Lesson lesson) {
        lessonId = lesson.getLessonId();
        lessonDate = lesson.getLessonDate();
        startTime = lesson.getStartTime();
        endTime = lesson.getEndTime();
        participantNumber = lesson.getParticipantNumber();
    }

    public LessonDto(Lesson lesson, int personal) {
        lessonId = lesson.getLessonId();
        lessonDate = lesson.getLessonDate();
        startTime = lesson.getStartTime();
        endTime = lesson.getEndTime();
        participantNumber = lesson.getParticipantNumber();
        this.personal = personal;
    }

    public record Request(
            @Schema(description = "레슨 날짜", example = "2024-06-30")
            @NotNull
            LocalDate lessonDate,
            @Schema(description = "레슨 시간", example = "18:00:00")
            @NotNull
            LocalTime startTime
    ) {
        public Lesson toEntity(OneDayClass oneDayClass) {
            return Lesson.builder()
                    .lessonDate(lessonDate)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(oneDayClass.getDuration()))
                    .participantNumber(0)
                    .oneDayClass(oneDayClass)
                    .build();
        }
    }

}
