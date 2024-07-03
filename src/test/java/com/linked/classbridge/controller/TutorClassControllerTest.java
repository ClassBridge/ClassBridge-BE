package com.linked.classbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.ClassFAQRepository;
import com.linked.classbridge.repository.ClassTagRepository;
import com.linked.classbridge.repository.LessonRepository;
import com.linked.classbridge.repository.OneDayClassRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.OneDayClassService;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.service.TutorService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@WebMvcTest(TutorController.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class TutorClassControllerTest {

    @MockBean
    private OneDayClassService classService;

    @MockBean
    private TutorService tutorService;

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

//    @Test
//    @WithMockUser
//    void registerClass() throws Exception {
//        // given
//        User mockUser = User.builder().email("example@example.com").tutorId(1L).build();
//
//        // 파일들을 포함한 요청 객체 생성
//        ClassDto.ClassRequest request = ClassDto.ClassRequest.builder()
//                .className("클래스 이름")
//                .address1("서울특별시")
//                .address2("송파구")
//                .address3("올림픽로 300 롯데타워 1층")
//                .duration(90)
//                .price(45000)
//                .personal(4)
//                .hasParking(true)
//                .introduction("클래스 소개글 입니다.")
//                .startDate(LocalDate.of(2024, 6, 29))
//                .endDate(LocalDate.of(2024, 7, 1))
//                .categoryType(CategoryType.FITNESS)
//                .lesson(
//                        RepeatClassDto.builder()
//                                .mon(dayList.builder().times(Arrays.asList(LocalTime.of(14, 0, 0), LocalTime.of(19, 30, 0))).build())
//                                .build()
//                )
//                .faqList(Arrays.asList(
//                        ClassFAQ.builder().title("faq 제목1").content("faq 내용").build(),
//                        ClassFAQ.builder().title("faq 제목2").content("faq 내용").build()
//                ))
//                .tagList(Arrays.asList(
//                        ClassTag.builder().name("태그 이름1").build(),
//                        ClassTag.builder().name("태그 이름2").build()
//                ))
//                .build();
//
//        // 요청 객체를 JSON으로 직렬화
//        String requestJson = objectMapper.writeValueAsString(request);
//
//        // 테스트에서 반환될 응답 객체 생성
//        ClassDto.ClassResponse response = new ClassResponse(
//                1L, // classId
//                "클래스 이름",
//                "서울특별시", // address1
//                "송파구",     // address2
//                "올림픽로 300 롯데타워 1층", // address3
//                37.5137129859207, // latitude
//                127.104301829165, // longitude
//                90,                // timeTaken
//                45000,             // price
//                4,
//     0.0,               // totalStarRate
//                0,                 // totalReviews
//                true,       // parkingInformation
//                "클래스 소개글 입니다.", // introduction
//                LocalDate.of(2024, 6, 29), // startDate
//                LocalDate.of(2024, 7, 1), // endDate
//                CategoryType.FITNESS, // category
//                1L, // tutorId
//                new ArrayList<>(),
//                Arrays.asList(
//                        new LessonDto(1L, LocalDate.of(2024, 7, 1), LocalTime.of(14, 0, 0), LocalTime.of(15, 30, 0), 0),
//                        new LessonDto(2L, LocalDate.of(2024, 7, 1), LocalTime.of(19, 30, 0), LocalTime.of(21, 0, 0), 0)
//                ),
//                Arrays.asList(
//                        new ClassFAQDto(1L, "faq 제목1", "faq 내용"),
//                        new ClassFAQDto(2L, "faq 제목2", "faq 내용")
//                ),
//                Arrays.asList(
//                        new ClassTagDto(1L, "태그 이름1"),
//                        new ClassTagDto(2L, "태그 이름2")
//                )
//        );
//
//        // Mocking: classService.registerClass 메서드가 호출되면 위에서 생성한 응답 객체를 반환하도록 설정
//        given(classService.registerClass(tutor.getEmail(), request, new ArrayList<>())).willReturn(response);
//
//        mockMvc.perform(MockMvcRequestBuilders.
//                        multipart(HttpMethod.POST,"/api/tutors/class")
//                                .file(new MockMultipartFile("request", "", "application/json", requestJson.getBytes(
//                                        StandardCharsets.UTF_8)))
//                                .contentType("multipart/form-data")
//                                .accept(MediaType.APPLICATION_JSON)
//                                .characterEncoding("UTF-8")
//                                .with(csrf())
//                )
//                .andExpect(status().isCreated()) // HTTP 상태코드가 201(CREATED)인지 확인
//                .andExpect(jsonPath("$.message").value("클래스 등록 성공")) // 응답 JSON에서 메시지 확인
//                .andExpect(jsonPath("$.data.className").value("클래스 이름")) // 응답 JSON에서 클래스 이름 확인
//                .andExpect(jsonPath("$.data.classId").value(1L));
//    }
//
//    @Test
//    @WithMockUser
//    void registerClass_whenUserNotFound_shouldReturnUserNotFound() throws Exception {
//        // given
//        ClassDto.ClassRequest request = ClassDto.ClassRequest.builder()
//                .className("클래스 이름")
//                .address1("서울특별시")
//                .address2("송파구")
//                .address3("올림픽로 300 롯데타워 1층")
//                .duration(90)
//                .price(45000)
//                .personal(4)
//                .hasParking(true)
//                .introduction("클래스 소개글 입니다.")
//                .startDate(LocalDate.of(2024, 6, 29))
//                .endDate(LocalDate.of(2024, 7, 1))
//                .categoryType(CategoryType.FITNESS)
//                .lesson(
//                        RepeatClassDto.builder()
//                                .mon(dayList.builder().times(Arrays.asList(LocalTime.of(14, 0, 0), LocalTime.of(19, 30, 0))).build())
//                                .build()
//                )
//                .faqList(Arrays.asList(
//                        ClassFAQ.builder().title("faq 제목1").content("faq 내용").build(),
//                        ClassFAQ.builder().title("faq 제목2").content("faq 내용").build()
//                ))
//                .tagList(Arrays.asList(
//                        ClassTag.builder().name("태그 이름1").build(),
//                        ClassTag.builder().name("태그 이름2").build()
//                ))
//                .build();
//
//        // 요청 객체를 JSON으로 직렬화
//        String requestJson = objectMapper.writeValueAsString(request);
//
//        // Mocking: userService.getCurrentUserEmail()이 USER_NOT_FOUND 예외를 던지도록 설정
//        given(userService.getCurrentUserEmail()).willThrow(new RestApiException(ErrorCode.USER_NOT_FOUND));
//
//        mockMvc.perform(MockMvcRequestBuilders.
//                        multipart(HttpMethod.POST,"/api/tutors/class")
//                        .file(new MockMultipartFile("request", "", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)))
//                        .contentType("multipart/form-data")
//                        .accept(MediaType.APPLICATION_JSON)
//                        .characterEncoding("UTF-8")
//                        .with(csrf())
//                )
//                .andExpect(status().isNotFound()) // HTTP 상태코드가 404(NOT FOUND)인지 확인
//                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND")) // 응답 JSON에서 에러 코드 확인
//                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다.")); // 응답 JSON에서 에러 메시지 확인
//    }


//    @Test
//    @WithMockUser
//    @DisplayName("클래스 수정 성공")
//    void updateClass_success() throws Exception {
//        // Given
//        User mockUser = User.builder().email("example@example.com").userId(1L).build();
//        Category mockCategory = Category.builder().name(CategoryType.COOKING).build();
//        OneDayClass mockBeforeClass = OneDayClass.builder()
//                .classId(6L)
//                .startDate(LocalDate.of(2024,6,5))
//                .className("클래스 이름").tutor(mockUser)
//                .endDate(LocalDate.of(2024,8,30))
//                .totalStarRate(0.0)
//                .totalReviews(0)
//                .duration(50)
//                .personal(5)
//                .build();
//
//        ClassUpdateDto.ClassRequest request = ClassUpdateDto.ClassRequest
//                .builder()
//                .className("클래스 이름 수정")
//                .categoryType(CategoryType.COOKING)
//                .hasParking(false)
//                .price(50000)
//                .personal(4)
//                .startDate(LocalDate.of(2024,6,5))
//                .endDate(LocalDate.of(2024,8,1))
//                .duration(90)
//                .introduction("클래스 수정 설명입니다. 20글자 채우기 힘들어요.")
//                .address1("서울특별시")
//                .address2("송파구")
//                .address3("올림픽로 300 롯데타워 2층")
//                .build();
//
//        ClassUpdateDto.ClassResponse expectedResponse =
//                ClassUpdateDto.ClassResponse
//                        .builder()
//                        .className("클래스 이름 수정")
//                        .categoryType(mockCategory.getName())
//                        .hasParking(false)
//                        .price(50000)
//                        .personal(4)
//                        .startDate(LocalDate.of(2024,6,5))
//                        .endDate(LocalDate.of(2024,8,1))
//                        .duration(90)
//                        .totalReviews(0)
//                        .totalStarRate(0D)
//                        .introduction("클래스 수정 설명입니다. 20글자 채우기 힘들어요.")
//                        .address1("서울특별시")
//                        .address2("송파구")
//                        .address3("올림픽로 300 롯데타워 2층")
//                        .userId(1L)
//                        .classId(6L)
//                        .build();
//
//        List<MultipartFile> fileList = new ArrayList<>();
//
//        given(userRepository.findByEmail(mockUser.getEmail())).willReturn(Optional.of(mockUser));
//        given(categoryRepository.findByName(mockCategory.getName())).willReturn(mockCategory);
//        given(classRepository.findById(mockBeforeClass.getClassId())).willReturn(Optional.of(mockBeforeClass));
//        given(userService.getCurrentUserEmail()).willReturn(mockUser.getEmail());
//        given(userRepository.findByEmail(mockUser.getEmail())).willReturn(Optional.of(mockUser));
//
//        // When
//        given(classService.updateClass(mockUser.getEmail(), request, fileList, 6L)).willReturn(expectedResponse);
//
//        // Then
//        mockMvc.perform(put("/api/tutors/class/{classId}", 6L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\n"
//                                + "\t\"className\": \"클래스 이름 수정\",\n"
//                                + "\t\"address1\": \"서울특별시\",\n"
//                                + "\t\"address2\": \"송파구\",\n"
//                                + "\t\"address3\": \"올림픽로 300 롯데타워 2층\",\n"
//                                + "\t\"duration\": 90,\n"
//                                + "\t\"price\": 50000,\n"
//                                + "\t\"personal\": 4,\n"
//                                + "\t\"hasParking\": false,\n"
//                                + "\t\"introduction\": \"클래스 수정 설명입니다. 20글자 채우기 힘들어요.\",\n"
//                                + "\t\"startDate\": \"2024-06-05\",\n"
//                                + "\t\"endDate\": \"2024-08-01\",\n"
//                                + "\t\"categoryType\": \"COOKING\"\n"
//                                + "}")
//                        .with(csrf()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value("SUCCESS"))
//                .andExpect(jsonPath("$.data.className").value(expectedResponse.className()));
//    }

}
