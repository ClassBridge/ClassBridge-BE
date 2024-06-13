package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.CLASS_HAVE_MAX_TAG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassFAQDto;
import com.linked.classbridge.dto.oneDayClass.ClassTagDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassFAQRepository;
import com.linked.classbridge.repository.ClassImageRepository;
import com.linked.classbridge.repository.ClassTagRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private ClassTagRepository tagRepository;

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
    void registerTag() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).build();

        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(classRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));

        ClassTagDto request = ClassTagDto.builder().name("tag입니다.").build();

        ClassTag responseTag = ClassTag.builder()
                .tagId(1L)
                .name(request.getName())
                .oneDayClass(oneDayClass)
                .build();

        // When
        given(tagRepository.save(any(ClassTag.class))).willReturn(responseTag);

        // Execute the service method
        ClassTagDto response = oneDayClassService.registerTag(tutor.getEmail(), request, 1L);

        // Then
        assertThat(response.getTagId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo(request.getName());
    }

    @Test
    void registerTag_Fail_Max_Tag() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).build();

        List<ClassTag> list = Arrays.asList(
                ClassTag.builder().tagId(1L).oneDayClass(oneDayClass).build(),
                ClassTag.builder().tagId(2L).oneDayClass(oneDayClass).build(),
                ClassTag.builder().tagId(3L).oneDayClass(oneDayClass).build(),
                ClassTag.builder().tagId(4L).oneDayClass(oneDayClass).build(),
                ClassTag.builder().tagId(5L).oneDayClass(oneDayClass).build()
        );

        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(classRepository.findById(oneDayClass.getClassId())).willReturn(Optional.of(oneDayClass));
        given(tagRepository.findAllByOneDayClassClassId(oneDayClass.getClassId())).willReturn(list);

        ClassTagDto request = ClassTagDto.builder().name("tag입니다.").build();

        // When
        RestApiException exception = assertThrows(RestApiException.class,
                () -> oneDayClassService.registerTag(tutor.getEmail(), request, oneDayClass.getClassId()));

        // Then
        assertEquals(CLASS_HAVE_MAX_TAG, exception.getErrorCode());
    }


    @Test
    void updateTag() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).build();

        ClassTagDto request = ClassTagDto.builder().name("tag 수정").build();

        ClassTag originTag = ClassTag.builder()
                .tagId(1L)
                .name("tag")
                .oneDayClass(oneDayClass)
                .build();

        ClassTag responseTag = ClassTag.builder()
                .tagId(1L)
                .name("tag 수정")
                .oneDayClass(oneDayClass)
                .build();

        // When
        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(tagRepository.findById(1L)).willReturn(Optional.of(originTag));
        given(tagRepository.save(any(ClassTag.class))).willReturn(responseTag);

        // Execute the service method
        ClassTagDto response = oneDayClassService.updateTag(tutor.getEmail(), request, 1L, 1L);

        // Then
        assertThat(response.getTagId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo(request.getName());
    }

    @Test
    void deleteTag() {
        // Given
        User tutor = User.builder().userId(1L).email("example@example.com").build();
        OneDayClass oneDayClass = OneDayClass.builder().classId(1L).tutor(tutor).build();

        // When
        given(userRepository.findByEmail(tutor.getEmail())).willReturn(Optional.of(tutor));
        given(tagRepository.findById(1L)).willReturn(Optional.of(ClassTag.builder().tagId(1L).oneDayClass(oneDayClass).build()));

        // Execute the service method
        boolean response = oneDayClassService.deleteTag(tutor.getEmail(), 1L, 1L);

        // Then
        assertThat(response).isEqualTo(true);
    }
}