package com.linked.classbridge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.ClassFAQ;
import com.linked.classbridge.domain.ClassTag;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.oneDayClass.ClassDto;
import com.linked.classbridge.dto.oneDayClass.ClassDto.ClassResponse;
import com.linked.classbridge.dto.oneDayClass.ClassFAQDto;
import com.linked.classbridge.dto.oneDayClass.ClassTagDto;
import com.linked.classbridge.dto.oneDayClass.ClassUpdateDto;
import com.linked.classbridge.dto.oneDayClass.LessonDto;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto;
import com.linked.classbridge.dto.oneDayClass.RepeatClassDto.dayList;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassFAQRepository;
import com.linked.classbridge.repository.ClassTagRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.OneDayClassService;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.CategoryType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

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

    @MockBean
    private OneDayClassRepository classRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    LessonRepository lessonRepository;

    @MockBean
    ClassFAQRepository classFAQRepository;

    @MockBean
    ClassTagRepository classTagRepository;

    private User tutor;
    private Category category;


    @BeforeEach
    void setUp() {
        tutor = User.builder().username("최영근").nickname("닉네임").authType(AuthType.EMAIL).phone("010-1234-1234").userId(1L).email("example@example.com").password("1234").build();
        category = Category.builder().categoryId(1L).name(CategoryType.FITNESS).build();
    }

    @Test
    @WithMockUser
    void registerClass() throws Exception {
        // given
        User mockUser = User.builder().email("example@example.com").userId(1L).build();

        // 파일들을 포함한 요청 객체 생성
        ClassDto.ClassRequest request = ClassDto.ClassRequest.builder()
                .className("클래스 이름")
                .address1("서울특별시")
                .address2("송파구")
                .address3("올림픽로 300 롯데타워 1층")
                .duration(90)
                .price(45000)
                .personal(4)
                .hasParking(true)
                .introduction("클래스 소개글 입니다.")
                .startDate(LocalDate.of(2024, 6, 29))
                .endDate(LocalDate.of(2024, 7, 1))
                .categoryType(CategoryType.FITNESS)
                .lesson(
                        RepeatClassDto.builder()
                                .mon(dayList.builder().times(Arrays.asList(LocalTime.of(14, 0, 0), LocalTime.of(19, 30, 0))).build())
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
                4,
     0.0,               // totalStarRate
                0,                 // totalReviews
                true,       // parkingInformation
                "클래스 소개글 입니다.", // introduction
                LocalDate.of(2024, 6, 29), // startDate
                LocalDate.of(2024, 7, 1), // endDate
                CategoryType.FITNESS, // category
                1L, // userId
                new ArrayList<>(),
                Arrays.asList(
                        new LessonDto(1L, LocalDate.of(2024, 7, 1), LocalTime.of(14, 0, 0), LocalTime.of(15, 30, 0), 0),
                        new LessonDto(2L, LocalDate.of(2024, 7, 1), LocalTime.of(19, 30, 0), LocalTime.of(21, 0, 0), 0)
                ),
                Arrays.asList(
                        new ClassFAQDto(1L, "faq 제목1", "faq 내용"),
                        new ClassFAQDto(2L, "faq 제목2", "faq 내용")
                ),
                Arrays.asList(
                        new ClassTagDto(1L, "태그 이름1"),
                        new ClassTagDto(2L, "태그 이름2")
                )
        );

        // Mocking: classService.registerClass 메서드가 호출되면 위에서 생성한 응답 객체를 반환하도록 설정
        given(classService.registerClass(tutor.getEmail(), request, new ArrayList<>())).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.
                        multipart(HttpMethod.POST,"/api/tutors/class")
                                .file(new MockMultipartFile("request", "", "application/json", requestJson.getBytes(
                                        StandardCharsets.UTF_8)))
                                .contentType("multipart/form-data")
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .with(csrf())
                )
                .andExpect(status().isCreated()) // HTTP 상태코드가 201(CREATED)인지 확인
                .andExpect(jsonPath("$.message").value("클래스 등록 성공")) // 응답 JSON에서 메시지 확인
                .andExpect(jsonPath("$.data.className").value("클래스 이름")) // 응답 JSON에서 클래스 이름 확인
                .andExpect(jsonPath("$.data.classId").value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("클래스 수정 성공")
    void updateClass_success() throws Exception {
        // Given
        User mockUser = User.builder().email("example@example.com").userId(1L).build();
        Category mockCategory = Category.builder().name(CategoryType.COOKING).build();
        OneDayClass mockBeforeClass = OneDayClass.builder().classId(1L).className("클래스 이름").tutor(mockUser).build();

        ClassUpdateDto.ClassRequest request = ClassUpdateDto.ClassRequest
                .builder()
                .className("클래스 이름 수정")
                .categoryType(CategoryType.COOKING)
                .hasParking(false)
                .price(50000)
                .personal(4)
                .startDate(LocalDate.of(2024,6,5))
                .endDate(LocalDate.of(2024,8,1))
                .duration(90)
                .introduction("클래스 수정 설명입니다. 20글자 채우기 힘들어요.")
                .address1("서울특별시")
                .address2("송파구")
                .address3("올림픽로 300 롯데타워 2층")
                .build();

        ClassUpdateDto.ClassResponse response =
                ClassUpdateDto.ClassResponse
                        .builder()
                        .className("클래스 이름 수정")
                        .categoryType(mockCategory.getName())
                        .hasParking(false)
                        .price(50000)
                        .personal(4)
                        .startDate(LocalDate.of(2024,6,5))
                        .endDate(LocalDate.of(2024,8,1))
                        .duration(90)
                        .introduction("클래스 수정 설명입니다. 20글자 채우기 힘들어요.")
                        .address1("서울특별시")
                        .address2("송파구")
                        .address3("올림픽로 300 롯데타워 2층")
                        .userId(1L)
                        .classId(1L)
                        .build();

        given(userRepository.findById(mockUser.getUserId())).willReturn(Optional.of(mockUser));
        given(categoryRepository.findById(mockCategory.getCategoryId())).willReturn(Optional.of(mockCategory));
        given(classRepository.findById(mockBeforeClass.getClassId())).willReturn(Optional.of(mockBeforeClass));
        given(classService.updateClass(mockUser.getEmail(), request, 1L)).willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/tutors/class/{classId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n"
                                + "\t\"className\": \"클래스 이름 수정\",\n"
                                + "\t\"address1\": \"서울특별시\",\n"
                                + "\t\"address2\": \"송파구\",\n"
                                + "\t\"address3\": \"올림픽로 300 롯데타워 2층\",\n"
                                + "\t\"duration\": 90,\n"
                                + "\t\"price\": 50000,\n"
                                + "\t\"personal\": 4,\n"
                                + "\t\"hasParking\": false,\n"
                                + "\t\"introduction\": \"클래스 수정 설명입니다. 20글자 채우기 힘들어요.\",\n"
                                + "\t\"startDate\": \"2024-06-05\",\n"
                                + "\t\"endDate\": \"2024-08-01\",\n"
                                + "\t\"categoryType\": \"COOKING\"\n"
                                + "}")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.className").value(response.className()));
    }

    @Test
    @WithMockUser
    @DisplayName("클래스 삭제 성공")
    void deleteClass_success() throws Exception {
        // Given
        User mockUser = User.builder().userId(1L).email("example@example.com").build();

        given(userRepository.findById(mockUser.getUserId())).willReturn(Optional.of(mockUser));
        given(classService.deleteClass(mockUser.getEmail(), 1L)).willReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/tutors/class/{classId}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

}