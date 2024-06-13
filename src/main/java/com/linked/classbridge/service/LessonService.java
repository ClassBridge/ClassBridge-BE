package com.linked.classbridge.service;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.dto.oneDayClass.LessonDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;

    public Lesson findLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RestApiException(ErrorCode.LESSON_NOT_FOUND));
    }

    public LessonDto findLessonDto(Long lessonId) {
        Lesson lesson = findLessonById(lessonId);

        return new LessonDto(lesson, lesson.getOneDayClass().getPersonal());
    }
}
