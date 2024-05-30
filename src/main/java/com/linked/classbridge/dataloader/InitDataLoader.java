package com.linked.classbridge.dataloader;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    private final OneDayClassRepository oneDayClassRepository;

    private final LessonRepository lessonRepository;


    @Override
    public void run(String... args) throws Exception {
        User savedUser = userRepository.findById(1L)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@mail.com")
                        .password("admin")
                        .nickname("admin")
                        .build()));

        OneDayClass saveOneDayClass = oneDayClassRepository.findById(1L)
                .orElseGet(() -> oneDayClassRepository.save(OneDayClass.builder()
                        .tutor(savedUser)
                        .totalReviews(0)
                        .totalStarRate(0D)
                        .build()));

        Lesson savedLesson = lessonRepository.findById(1L)
                .orElseGet(() -> lessonRepository.save(Lesson.builder()
                        .oneDayClass(saveOneDayClass)
                        .build()));
    }
}
