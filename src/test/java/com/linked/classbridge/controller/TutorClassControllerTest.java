package com.linked.classbridge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.ClassDto.ClassResponse;
import com.linked.classbridge.dto.oneDayClass.ClassFAQDto;
import com.linked.classbridge.dto.oneDayClass.ClassTagDto;
import com.linked.classbridge.dto.oneDayClass.LessonDto;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto.dayList;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.OneDayClassService;
import com.linked.classbridge.type.CategoryType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TutorController.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class TutorClassControllerTest {

    @MockBean
    private OneDayClassService classService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        User user = User.builder().userId(1L).email("example@example.com").password("1234").build();
        Category category = Category.builder().categoryId(1L).name(CategoryType.FITNESS).sequence(1).build();

        // Mock the save and findById methods
        given(userRepository.save(any(User.class))).willReturn(user);
        given(categoryRepository.save(any(Category.class))).willReturn(category);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
    }

    @Test
    void registerClass() throws Exception {
        User user = User.builder().userId(1L).email("example@example.com").password("1234").build();
        Category category = Category.builder().categoryId(1L).name(CategoryType.FITNESS).sequence(1).build();

        given(userRepository.findByEmail("example@example.com")).willReturn(Optional.of(user));
        given(categoryRepository.findByName(CategoryType.FITNESS)).willReturn(category);

        // 파일들을 포함한 요청 객체 생성
        ClassDto.ClassRequest request = ClassDto.ClassRequest.builder()
                .className("클래스 이름")
                .address1("서울특별시")
                .address2("송파구")
                .address3("올림픽로 300 롯데타워 1층")
                .timeTaken(90)
                .price(45000)
                .parkingInformation("주차장 정보")
                .introduction("클래스 소개글 입니다.")
                .startDate(LocalDate.of(2024, 5, 30))
                .endDate(LocalDate.of(2024, 7, 31))
                .categoryType(CategoryType.FITNESS)
                .repeatClassDto(
                        RepeatClassDto.builder()
                                .mon(dayList.builder().times(Arrays.asList(LocalTime.of(18, 0, 0), LocalTime.of(19, 30, 0))).personal(6).build())
                                .build()
                )
                .faqList(Arrays.asList(
                        ClassFAQ.builder().title("faq 제목1").content("faq 내용").build(),
                        ClassFAQ.builder().title("faq 제목2").content("faq 내용").build()
                ))
                .tagList(Arrays.asList(
                        ClassTag.builder().name("태그 이름1").build(),
                        ClassTag.builder().name("태그 이름2").build()
                ))
                .build();

        // 요청 객체를 JSON으로 직렬화
        String requestJson = objectMapper.writeValueAsString(request);

        // 테스트에서 반환될 응답 객체 생성
        ClassDto.ClassResponse response = new ClassResponse(
                1L, // classId
                "클래스 이름",
                "서울특별시", // address1
                "송파구",     // address2
                "올림픽로 300 롯데타워 1층", // address3
                37.5137129859207, // latitude
                127.104301829165, // longitude
                90,                // timeTaken
                45000,             // price
                0.0,               // totalStarRate
                0,                 // totalReviews
                "주차장 정보",       // parkingInformation
                "클래스 소개글 입니다.", // introduction
                LocalDate.of(2024, 5, 30), // startDate
                LocalDate.of(2024, 7, 31), // endDate
                new Category(1L, 1, CategoryType.FITNESS), // category
                1L, // userId
                Collections.emptyList(),
                Arrays.asList(
                        new LessonDto(1L, LocalDate.of(2024, 6, 3), LocalTime.of(14, 0, 0), LocalTime.of(15, 0, 0), 6, 0),
                        new LessonDto(2L, LocalDate.of(2024, 6, 3), LocalTime.of(18, 0, 0), LocalTime.of(19, 0, 0), 6, 0)
                ),
                Arrays.asList(
                        new ClassFAQDto(1L, "faq 제목1", "faq 내용", 1),
                        new ClassFAQDto(2L, "faq 제목2", "faq 내용", 2)
                ),
                Arrays.asList(
                        new ClassTagDto(1L, "태그 이름1"),
                        new ClassTagDto(2L, "태그 이름2")
                )
        );

        // Mocking: classService.registerClass 메서드가 호출되면 위에서 생성한 응답 객체를 반환하도록 설정
        given(classService.registerClass(user, request, new ArrayList<>())).willReturn(response);

        mockMvc.perform(
                        multipart("/api/tutors/class")
                                .file(new MockMultipartFile("request", "", "application/json", requestJson.getBytes(
                                        StandardCharsets.UTF_8)))
                                .contentType("multipart/form-data")
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                )
                .andExpect(status().isCreated()) // HTTP 상태코드가 201(CREATED)인지 확인
                .andExpect(jsonPath("$.message").value("클래스 등록 성공")) // 응답 JSON에서 메시지 확인
                .andExpect(jsonPath("$.data.className").value("클래스 이름")) // 응답 JSON에서 클래스 이름 확인
                .andExpect(jsonPath("$.data.classId").value(1L));
    }

}
