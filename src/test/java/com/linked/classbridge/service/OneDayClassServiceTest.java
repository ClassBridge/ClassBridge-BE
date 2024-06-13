package com.linked.classbridge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassFAQDto;
import com.linked.classbridge.dto.oneDayClass.LessonDto;
import com.linked.classbridge.dto.oneDayClass.LessonDto.Request;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassFAQRepository;
import com.linked.classbridge.repository.ClassImageRepository;
import com.linked.classbridge.repository.ClassTagRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class OneDayClassServiceTest {

    @InjectMocks
    private OneDayClassService oneDayClassService;

    @Mock
    private OneDayClassRepository classRepository;

    @Mock
    private UserRepository userRepository;

    @MockBean
    private KakaoMapService kakaoMapService;

    @Mock
    private CategoryRepository categoryRepository;

    @MockBean
    private S3Service s3Service;

    @Mock
    private ClassImageRepository classImageRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ClassFAQRepository faqRepository;

    @Mock
    private ClassTagRepository classTagRepository;

    @Test
    void registerFAQ() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).build();

        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(classRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));
        given(faqRepository.findAllByOneDayClassClassId(oneDayClass.getClassId())).willReturn(new ArrayList<>());

        ClassFAQDto request = ClassFAQDto.builder().title("faq 제목입니다.").content("faq 내용입니다.").build();

        ClassFAQ responseFaq = ClassFAQ.builder()
                .faqId(1L)
                .title(request.getTitle())
                .content(request.getContent())
                .oneDayClass(oneDayClass)
                .build();

        // When
        given(faqRepository.save(any(ClassFAQ.class))).willReturn(responseFaq);

        // Execute the service method
        ClassFAQDto response = oneDayClassService.registerFAQ(tutor.getEmail(), request, 1L);

        // Then
        assertThat(response.getFaqId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getContent()).isEqualTo(request.getContent());
    }

    @Test
    void updateFAQ() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).build();

        ClassFAQDto request = ClassFAQDto.builder().title("faq 수정 제목입니다.").content("faq 수정 내용입니다.").build();

        ClassFAQ responseFaq = ClassFAQ.builder()
                .faqId(1L)
                .title("faq 수정 제목입니다.")
                .content("faq 수정 내용입니다.")
                .oneDayClass(oneDayClass)
                .build();

        // When
        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(classRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));
        given(faqRepository.save(any(ClassFAQ.class))).willReturn(responseFaq);

        // Execute the service method
        ClassFAQDto response = oneDayClassService.registerFAQ(tutor.getEmail(), request, 1L);

        // Then
        assertThat(response.getFaqId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getContent()).isEqualTo(request.getContent());
    }

    @Test
    void deleteFAQ() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).build();

        // When
        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(faqRepository.findById(1L)).willReturn(Optional.of(ClassFAQ.builder().faqId(1L).oneDayClass(oneDayClass).build()));

        // Execute the service method
        boolean response = oneDayClassService.deleteFAQ(tutor.getEmail(), 1L, 1L);

        // Then
        assertThat(response).isEqualTo(true);
    }

    @Test
    void registerLesson() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).duration(90).build();

        LessonDto.Request request = new Request(LocalDate.now().plusDays(1), LocalTime.of(10,0,0));

        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(classRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));
        given(lessonRepository.existsByOneDayClassClassIdAndLessonDateAndStartTime(oneDayClass.getClassId(), request.lessonDate(), request.startTime())).willReturn(false);

        Lesson lesson = Lesson.builder()
                        .lessonId(1L)
                        .lessonDate(request.lessonDate())
                        .startTime(request.startTime())
                        .endTime(request.startTime().plusMinutes(oneDayClass.getDuration()))
                        .oneDayClass(oneDayClass)
                        .participantNumber(0)
                        .build();

        given(lessonRepository.save(any(Lesson.class))).willReturn(lesson);
        // When
        LessonDto response = oneDayClassService.registerLesson(tutor.getEmail(), request, 1L);

        // Then
        assertThat(response.getLessonId()).isEqualTo(1L);
        assertThat(response.getLessonDate()).isEqualTo(request.lessonDate());
        assertThat(response.getEndTime()).isEqualTo(request.startTime().plusMinutes(oneDayClass.getDuration()));
    }

    @Test
    void updateLesson() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).duration(90).build();

        LessonDto.Request request = new Request(LocalDate.now().plusDays(1), LocalTime.of(10,0,0));

        Lesson beforeLesson = Lesson.builder()
                .lessonId(1L)
                .lessonDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(18, 0, 0))
                .oneDayClass(oneDayClass)
                .participantNumber(0)
                .build();

        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(lessonRepository.findById(1L)).willReturn(Optional.of(beforeLesson));
        given(lessonRepository.existsByOneDayClassClassIdAndLessonDateAndStartTime(oneDayClass.getClassId(), request.lessonDate(), request.startTime())).willReturn(false);

        Lesson afterLesson = Lesson.builder()
                .lessonId(1L)
                .lessonDate(request.lessonDate())
                .startTime(request.startTime())
                .endTime(request.startTime().plusMinutes(oneDayClass.getDuration()))
                .oneDayClass(oneDayClass)
                .participantNumber(0)
                .build();

        given(lessonRepository.save(any(Lesson.class))).willReturn(afterLesson);

        // When
        LessonDto response = oneDayClassService.updateLesson(tutor.getEmail(), request, 1L, 1L);

        // Then
        assertThat(response.getLessonId()).isEqualTo(1L);
        assertThat(response.getLessonDate()).isEqualTo(request.lessonDate());
        assertThat(response.getEndTime()).isEqualTo(request.startTime().plusMinutes(oneDayClass.getDuration()));
    }

    @Test
    void deleteLesson() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).duration(90).build();

        Lesson lesson = Lesson.builder()
                .lessonId(1L)
                .lessonDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(18, 0, 0))
                .oneDayClass(oneDayClass)
                .participantNumber(0)
                .build();

        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(lessonRepository.findById(1L)).willReturn(Optional.of(lesson));

        // When
        boolean response = oneDayClassService.deleteLesson(tutor.getEmail(),1L, 1L);

        // Then
        assertThat(response).isEqualTo(true);
    }
}