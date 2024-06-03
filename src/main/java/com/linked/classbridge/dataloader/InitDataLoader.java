package com.linked.classbridge.dataloader;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassFAQRepository;
import com.linked.classbridge.repository.ClassTagRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.CategoryType;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    private final OneDayClassRepository oneDayClassRepository;

    private final LessonRepository lessonRepository;

    private final CategoryRepository categoryRepository;

    private final ClassFAQRepository faqRepository;

    private final ClassTagRepository tagRepository;


    @Override
    public void run(String... args) throws Exception {
        User savedUser = userRepository.findById(1L)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin@mail.com")
                        .password("admin")
                        .nickname("admin")
                        .build()));

        Category category = categoryRepository.findById(1L)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(CategoryType.FITNESS)
                        .sequence(1)
                        .build()));

        OneDayClass saveOneDayClass = oneDayClassRepository.findById(1L)
                .orElseGet(() -> oneDayClassRepository.save(OneDayClass.builder()
                        .tutor(savedUser)
                        .className("클래스 이름입니다.")
                        .price(30000)
                        .timeTaken(60)
                        .startDate(LocalDate.of(2024,5,31))
                        .endDate(LocalDate.of(2024,6,30))
                        .parkingInformation("주차장 정보")
                        .introduction("강의 소개글 입니다. 많이 찾아와주세요.")
                        .address1("서울특별시")
                        .address2("중구")
                        .address3("세종대로 110 서울특별시청 1층")
                        .latitude(37.56680168084819)
                        .longitude(126.97866924383023)
                        .totalReviews(0)
                        .totalStarRate(0D)
                        .category(category)
                        .build()));

        Lesson savedLesson = lessonRepository.findById(1L)
                .orElseGet(() -> lessonRepository.save(Lesson.builder()
                        .oneDayClass(saveOneDayClass)
                        .startTime(LocalTime.of(15,0))
                        .endTime(LocalTime.of(16,0))
                        .personnel(6)
                        .participantNumber(0)
                        .lessonDate(LocalDate.now())
                        .build()));

        ClassTag saveTag = tagRepository.findById(1L)
                .orElseGet(() -> tagRepository.save(ClassTag.builder()
                        .name("태그 1")
                        .oneDayClass(saveOneDayClass)
                        .build()));

        ClassFAQ saveFaq = faqRepository.findById(1L)
                .orElseGet(() -> faqRepository.save(ClassFAQ.builder()
                        .title("faq 제목")
                        .content("faq 내용입니다.")
                        .oneDayClass(saveOneDayClass)
                        .sequence(1)
                        .build()));
    }
}
