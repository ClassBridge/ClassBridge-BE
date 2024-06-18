package com.linked.classbridge.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.dto.tutor.TutorInfoDto;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.OneDayClassService;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.service.TutorService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.ResponseMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(TutorController.class)
@AutoConfigureMockMvc
//@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class TutorControllerTest {

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private TutorService tutorService;

    @MockBean
    private UserService userService;

    @MockBean
    private OneDayClassService oneDayClassService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private Pageable pageable;

    private User mockTutor1;
    private GetReviewResponse reviewResponse1;
    private GetReviewResponse reviewResponse2;
    private GetReviewResponse reviewResponse3;
    private GetReviewResponse reviewResponse4;

    @BeforeEach
    void setUp() {
        mockTutor1 = User.builder()
                .userId(1L)
                .nickname("tutorNickname")
                .build();

        pageable = PageRequest.of(0, 5, Direction.DESC, "createdAt");

        reviewResponse1 = new GetReviewResponse(
                1L, 1L, "className", 1L, 1L,
                "userNickname", 4.5, "review1 content",
                LocalDate.now(), LocalDateTime.now(), List.of("url1", "url2", "url3")
        );
        reviewResponse2 = new GetReviewResponse(
                1L, 1L, "className", 2L, 1L,
                "userNickname", 4.5, "review1 content",
                LocalDate.now(), LocalDateTime.now(), List.of("url1", "url2", "url3")
        );
        reviewResponse3 = new GetReviewResponse(
                1L, 2L, "className", 3L, 2L,
                "userNickname", 4.5, "review1 content",
                LocalDate.now(), LocalDateTime.now(), List.of("url1", "url2", "url3")
        );
        reviewResponse4 = new GetReviewResponse(
                1L, 2L, "className", 4L, 2L,
                "userNickname", 4.5, "review1 content",
                LocalDate.now(), LocalDateTime.now(), List.of("url1", "url2", "url3")
        );
    }

    @Test
    @DisplayName("강사 받은 리뷰 목록 조회")
    void getUserReviews() throws Exception {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(mockTutor1));
        given(reviewService.getTutorReviews(mockTutor1.getEmail(), pageable)).willReturn(new PageImpl<>(
                Arrays.asList(reviewResponse1, reviewResponse2, reviewResponse3, reviewResponse4)
                , pageable, 4)
        );

        // when & then
        mockMvc.perform(get("/api/tutors/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value(
                        ResponseMessage.REVIEW_GET_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.content[0].reviewId").value(reviewResponse1.reviewId()))
                .andExpect(jsonPath("$.data.content[0].classId").value(reviewResponse1.classId()))
                .andExpect(jsonPath("$.data.content[0].lessonId").value(reviewResponse1.lessonId()))
                .andExpect(jsonPath("$.data.content[0].userId").value(reviewResponse1.userId()))
                .andExpect(jsonPath("$.data.content[1].reviewId").value(reviewResponse2.reviewId()))
                .andExpect(jsonPath("$.data.content[1].classId").value(reviewResponse2.classId()))
                .andExpect(jsonPath("$.data.content[1].lessonId").value(reviewResponse2.lessonId()))
                .andExpect(jsonPath("$.data.content[1].userId").value(reviewResponse2.userId()))
                .andExpect(jsonPath("$.data.content[2].reviewId").value(reviewResponse3.reviewId()))
                .andExpect(jsonPath("$.data.content[2].classId").value(reviewResponse3.classId()))
                .andExpect(jsonPath("$.data.content[2].lessonId").value(reviewResponse3.lessonId()))
                .andExpect(jsonPath("$.data.content[2].userId").value(reviewResponse3.userId()))
                .andExpect(jsonPath("$.data.content[3].reviewId").value(reviewResponse4.reviewId()))
                .andExpect(jsonPath("$.data.content[3].classId").value(reviewResponse4.classId()))
                .andExpect(jsonPath("$.data.content[3].lessonId").value(reviewResponse4.lessonId()))
                .andExpect(jsonPath("$.data.content[3].userId").value(reviewResponse4.userId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("강사 등록 성공")
    public void registerTutor_success() throws Exception {

        TutorInfoDto tutorInfoDto = new TutorInfoDto();
        tutorInfoDto.setBank("국민은행");
        tutorInfoDto.setAccount("0123456789");
        tutorInfoDto.setBusinessRegistrationNumber("1234567890");
        tutorInfoDto.setIntroduction("강사 소개");

        when(tutorService.registerTutor(tutorInfoDto)).thenReturn("강사 등록 성공");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tutors/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tutorInfoDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("강사 등록 성공"));
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    @DisplayName("강사 정보 수정 성공")
    public void updateTutorInfo_success() throws Exception {

        TutorInfoDto tutorInfoDto = new TutorInfoDto();
        tutorInfoDto.setBank("국민은행");
        tutorInfoDto.setAccount("0123456789");
        tutorInfoDto.setBusinessRegistrationNumber("1234567890");
        tutorInfoDto.setIntroduction("강사 소개를 적어보고자 합니다");

        when(tutorService.updateTutorInfo(tutorInfoDto)).thenReturn("tutor info updated successfully");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/tutors/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tutorInfoDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("강사 정보 수정 성공"));
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    @DisplayName("출석체크 및 스템프 부여 성공")
    public void checkAttendance_success() throws Exception {

        Long userId = 1L;
        Long reservationId = 1L;
        String expectedResponse = "attendance checked and stamp issued successfully";

        when(tutorService.checkAttendance(userId, reservationId)).thenReturn(expectedResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tutors/check-attendance")
                        .param("userId", userId.toString())
                        .param("reservationId", reservationId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    @DisplayName("출석체크 및 스템프 부여 실패 - 예약 정보 없음")
    public void checkAttendance_failure_reservation_not_found() throws Exception {

        Long userId = 1L;
        Long reservationId = 1L;

        when(tutorService.checkAttendance(userId, reservationId)).thenThrow(new RestApiException(ErrorCode.RESERVATION_NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tutors/check-attendance")
                        .param("userId", userId.toString())
                        .param("reservationId", reservationId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    @DisplayName("출석체크 및 스템프 부여 실패 - 레슨 당일에만 출석체크 가능")
    public void checkAttendance_failure_not_today_lesson() throws Exception {

        Long userId = 1L;
        Long reservationId = 1L;

        when(tutorService.checkAttendance(userId, reservationId)).thenThrow(new RestApiException(ErrorCode.NOT_TODAY_LESSON));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tutors/check-attendance")
                        .param("userId", userId.toString())
                        .param("reservationId", reservationId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TUTOR")
    @DisplayName("출석체크 및 스템프 부여 실패 - 레슨 시작 30분 전부터 출석체크 가능")
    public void checkAttendance_failure_not_yet_attendance() throws Exception {

        Long userId = 1L;
        Long reservationId = 1L;

        when(tutorService.checkAttendance(userId, reservationId)).thenThrow(new RestApiException(ErrorCode.NOT_YET_ATTENDANCE));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tutors/check-attendance")
                        .param("userId", userId.toString())
                        .param("reservationId", reservationId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}