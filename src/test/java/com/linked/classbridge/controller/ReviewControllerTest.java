package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.NOT_REVIEW_OWNER;
import static com.linked.classbridge.type.ErrorCode.REVIEW_ALREADY_EXISTS;
import static com.linked.classbridge.type.ErrorCode.REVIEW_NOT_FOUND;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.DeleteReviewResponse;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.dto.review.UpdateReviewDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.service.UserService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

@WebMvcTest(ReviewController.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class ReviewControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    private MockMultipartFile image1;
    private MockMultipartFile image2;
    private MockMultipartFile image3;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1L)
                .email("user@mail.com")
                .build();

        given(userService.getCurrentUserEmail()).willReturn(mockUser.getEmail());
        given(userService.getUserByEmail(mockUser.getEmail())).willReturn(mockUser);

        image1 = new MockMultipartFile("image1", "image1.jpg", "image/jpeg",
                "image1 content".getBytes(StandardCharsets.UTF_8));
        image2 = new MockMultipartFile("image2", "image2.jpg", "image/jpeg",
                "image2 content".getBytes(StandardCharsets.UTF_8));
        image3 = new MockMultipartFile("image3", "image3.jpg", "image/jpeg",
                "image3 content".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("리뷰 등록 성공")
    @WithMockUser
    void registerReview_success() throws Exception {
        // Given
        RegisterReviewDto.Request request = new RegisterReviewDto.Request(
                1L, 1L, "This is a valid content.", 4.5, image1, image2, image3);

        RegisterReviewDto.Response response = new RegisterReviewDto.Response(1L);

        given(reviewService.registerReview(eq(mockUser), eq(request))).willReturn(response);
        // When & Then
        mockMvc.perform(multipart("/api/reviews")
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("lessonId", request.lessonId().toString())
                        .param("classId", request.classId().toString())
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(response.reviewId()));
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 레슨 ID 미입력")
    @WithMockUser
    void registerReview_fail_lessonId_not_enter() throws Exception {
        // Given
        RegisterReviewDto.Request request = new RegisterReviewDto.Request(
                1L, null, "This is a valid content.", 4.5, image1, image2, image3);
        RegisterReviewDto.Response response = new RegisterReviewDto.Response(1L);

        given(reviewService.registerReview(eq(mockUser), eq(request))).willReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/reviews")
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("classId", request.classId().toString())
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("lessonId"))
                .andExpect(jsonPath("$.errors[0].message").value("레슨 ID를 입력해 주세요."))
        ;
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 리뷰 내용 길이 미달")
    @WithMockUser
    void registerReview_fail_contents_too_short() throws Exception {
        // Given
        RegisterReviewDto.Request request
                = new RegisterReviewDto.Request(1L, 1L, "short", 4.5, image1, image2, image3);

        // When & Then
        mockMvc.perform(multipart("/api/reviews")
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("classId", request.classId().toString())
                        .param("lessonId", request.lessonId().toString())
                        .param("contents", "short") // 10자 미만
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("contents"))
                .andExpect(jsonPath("$.errors[0].message").value("리뷰 내용은 10자 이상 200자 이하로 입력해 주세요."))
        ;
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 평점 범위 초과")
    @WithMockUser
    void registerReview_fail_rating_out_of_range() throws Exception {
        // Given
        RegisterReviewDto.Request request = new RegisterReviewDto.Request(
                1L, 1L, "This is a valid content.", 7.0, image1, image2, image3);
        RegisterReviewDto.Response response = new RegisterReviewDto.Response(1L);

//        given(reviewService.registerReview(eq(mockUser), eq(request))).willReturn(response);

        // When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/reviews")
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("classId", request.classId().toString())
                        .param("lessonId", request.lessonId().toString())
                        .param("contents", request.contents()) // 10자 미만
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("rating"))
                .andExpect(jsonPath("$.errors[0].message").value("평점은 0 이상 5 이하로 입력해 주세요."))
        ;
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 이미 등록된 리뷰")
    @WithMockUser
    void registerReview_fail_already_register_review() throws Exception {
        // Given
        RegisterReviewDto.Request request = new RegisterReviewDto.Request(1L, 1L,
                "This is a valid content.", 4.5, image1, image2, image3);

        given(reviewService.registerReview(eq(mockUser), eq(request)))
                .willThrow(new RestApiException(REVIEW_ALREADY_EXISTS));

        // When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/reviews")
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("classId", request.classId().toString())
                        .param("lessonId", request.lessonId().toString())
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REVIEW_ALREADY_EXISTS.name()))
                .andExpect(jsonPath("$.message").value(REVIEW_ALREADY_EXISTS.getDescription()))
        ;

    }

    @Test
    @DisplayName("리뷰 수정 성공")
    @WithMockUser
    void updateReview_success() throws Exception {
        // Given
        UpdateReviewDto.Request request = new UpdateReviewDto.Request(
                "updated review contents", 4.5, image1, image2, image3);
        UpdateReviewDto.Response response =
                new UpdateReviewDto.Response(1L, "updated review contents", 4.5);

        given(reviewService.updateReview(mockUser, request, 1L)).willReturn(response);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(response.reviewId()))
                .andExpect(jsonPath("$.data.contents").value(response.contents()))
                .andExpect(jsonPath("$.data.rating").value(response.rating()))
        ;
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 내용 길이 미달")
    @WithMockUser
    void updateReview_fail_contents_too_short() throws Exception {
        // Given
        UpdateReviewDto.Request request = new UpdateReviewDto.Request(
                "short", 4.5, image1, image2, image3);
        UpdateReviewDto.Response response =
                new UpdateReviewDto.Response(1L, "updated review contents", 4.5);

        given(reviewService.updateReview(eq(mockUser), eq(request), eq(1L))).willReturn(response);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("contents"))
                .andExpect(jsonPath("$.errors[0].message").value("리뷰 내용은 10자 이상 200자 이하로 입력해 주세요."))
        ;
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 평점 범위 초과")
    @WithMockUser
    void updateReview_fail_rating_out_of_range() throws Exception {
        // Given
        UpdateReviewDto.Request request = new UpdateReviewDto.Request(
                "updated review contents", 10.0, image1, image2, image3);
        UpdateReviewDto.Response response =
                new UpdateReviewDto.Response(1L, "updated review contents", 4.5);

        given(reviewService.updateReview(eq(mockUser), eq(request), eq(1L))).willReturn(response);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("rating"))
                .andExpect(jsonPath("$.errors[0].message").value("평점은 0 이상 5 이하로 입력해 주세요."))
        ;
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 작성자가 아닌 사용자")
    @WithMockUser
    void updateReview_fail_not_review_owner() throws Exception {
        // Given
        UpdateReviewDto.Request request = new UpdateReviewDto.Request(
                "updated review contents", 4.5, image1, image2, image3);

        given(reviewService.updateReview(eq(mockUser), eq(request), eq(1L)))
                .willThrow(new RestApiException(NOT_REVIEW_OWNER));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(NOT_REVIEW_OWNER.name()))
                .andExpect(jsonPath("$.message").value(NOT_REVIEW_OWNER.getDescription()))
        ;
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재 하지 않는 리뷰 ID")
    @WithMockUser
    void updateReview_fail_not_exist_review_id() throws Exception {
        // Given
        UpdateReviewDto.Request request = new UpdateReviewDto.Request(
                "updated review contents", 4.5, image1, image2, image3);

        given(reviewService.updateReview(eq(mockUser), eq(request), eq(1L)))
                .willThrow(new RestApiException(REVIEW_NOT_FOUND));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file(image1)
                        .file(image2)
                        .file(image3)
                        .param("contents", request.contents())
                        .param("rating", request.rating().toString())
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REVIEW_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(REVIEW_NOT_FOUND.getDescription()))
        ;
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    @WithMockUser
    void deleteReview_success() throws Exception {
        // Given
        given(reviewService.deleteReview(eq(mockUser), eq(1L))).
                willReturn(new DeleteReviewResponse(1L));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/reviews/{reviewId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))

                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(1L))
        ;
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 리뷰 작성자가 아닌 사용자")
    @WithMockUser
    void deleteReview_fail_not_review_owner() throws Exception {
        // Given
        given(reviewService.deleteReview(eq(mockUser), eq(1L)))
                .willThrow(new RestApiException(NOT_REVIEW_OWNER));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/reviews/{reviewId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(NOT_REVIEW_OWNER.name()))
                .andExpect(jsonPath("$.message").value(NOT_REVIEW_OWNER.getDescription()))
        ;
    }

    @Test
    @DisplayName("리뷰 단일 조회 성공")
    @WithMockUser
    void getReview_success() throws Exception {
        GetReviewResponse response = new GetReviewResponse(
                1L,
                1L,
                "className",
                1L,
                1L,
                "userNickName",
                4.5,
                "contents",
                LocalDate.now(),
                LocalDateTime.now(),
                List.of("image1", "image2", "image3")
        );

        // Given
        given(reviewService.getReview(1L)).willReturn(response);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/reviews/{reviewId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(response.reviewId()))
                .andExpect(jsonPath("$.data.classId").value(response.classId()))
                .andExpect(jsonPath("$.data.className").value(response.className()))
                .andExpect(jsonPath("$.data.lessonId").value(response.lessonId()))
                .andExpect(jsonPath("$.data.userId").value(response.userId()))
                .andExpect(jsonPath("$.data.userNickName").value(response.userNickName()))
                .andExpect(jsonPath("$.data.rating").value(response.rating()))
                .andExpect(jsonPath("$.data.contents").value(response.contents()))
                .andExpect(jsonPath("$.data.reviewImageUrlList").isArray())
                .andExpect(jsonPath("$.data.reviewImageUrlList[0]").value(
                        response.reviewImageUrlList().get(0)))
                .andExpect(jsonPath("$.data.reviewImageUrlList[1]").value(
                        response.reviewImageUrlList().get(1)))
                .andExpect(jsonPath("$.data.reviewImageUrlList[2]").value(
                        response.reviewImageUrlList().get(2)));
    }

    @Test
    @DisplayName("리뷰 단일 조회 실패 - 존재 하지 않는 리뷰 ID")
    @WithMockUser
    void getReview_fail_not_exist_review_id() throws Exception {
        // Given
        given(reviewService.getReview(1L)).willThrow(new RestApiException(REVIEW_NOT_FOUND));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/reviews/{reviewId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REVIEW_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(REVIEW_NOT_FOUND.getDescription()));
    }

}