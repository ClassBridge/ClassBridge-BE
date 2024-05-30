package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.NOT_REVIEW_OWNER;
import static com.linked.classbridge.type.ErrorCode.REVIEW_ALREADY_EXISTS;
import static com.linked.classbridge.type.ErrorCode.REVIEW_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.DeleteReviewResponse;
import com.linked.classbridge.dto.review.RegisterReviewDto;
import com.linked.classbridge.dto.review.UpdateReviewDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.ReviewService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class ReviewControllerTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    MockMultipartFile image1;
    MockMultipartFile image2;
    MockMultipartFile image3;

    @BeforeEach
    void setUp() {
        image1 = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "image1".getBytes());
        image2 = new MockMultipartFile("images", "image2.jpg", "image/jpeg", "image2".getBytes());
        image3 = new MockMultipartFile("images", "image3.jpg", "image/jpeg", "image3".getBytes());

    }

    @Test
    @DisplayName("리뷰 등록 성공")
    void registerReview_success() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));
        given(reviewService.registerReview(any(User.class), any(RegisterReviewDto.Request.class)))
                .willReturn(new RegisterReviewDto.Response(1L));

        // When & Then
        mockMvc.perform(multipart("/api/reviews")
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("lessonId", "1")
                        .param("classId", "1")
                        .param("contents", "This is a valid content.")
                        .param("rating", "4.5")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(1L))
        ;
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 레슨 ID 미입력")
    void registerReview_fail_lessonId_not_enter() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));

        // When & Then
        mockMvc.perform(multipart("/api/reviews")
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("classId", "1")
                        .param("contents", "This is a valid content.")
                        .param("rating", "4.5")
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
    void registerReview_fail_contents_too_short() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));

        // When & Then
        mockMvc.perform(multipart("/api/reviews")
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("classId", "1")
                        .param("lessonId", "1")
                        .param("contents", "short") // 10자 미만
                        .param("rating", "4.5")
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
    void registerReview_fail_rating_out_of_range() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));

        // When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/reviews")
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("classId", "1")
                        .param("lessonId", "1")
                        .param("contents", "This is a valid content.")
                        .param("rating", "5.1") // 5 초과
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
    void registerReview_fail_already_register_review() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));
        given(reviewService.registerReview(any(User.class), any(RegisterReviewDto.Request.class)))
                .willThrow(new RestApiException(REVIEW_ALREADY_EXISTS));

        // When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/reviews")
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("classId", "1")
                        .param("lessonId", "1")
                        .param("contents", "This is a valid content.")
                        .param("rating", "4.5") // 5 초과
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REVIEW_ALREADY_EXISTS.name()))
                .andExpect(jsonPath("$.message").value(REVIEW_ALREADY_EXISTS.getDescription()))
        ;

    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_success() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));
        given(reviewService.updateReview(
                any(User.class), any(UpdateReviewDto.Request.class), any(Long.class)
        )).willReturn(new UpdateReviewDto.Response(1L, "updated review contents", 4.5));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("contents", "updated review contents")
                        .param("rating", "4.5")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(1L))
                .andExpect(jsonPath("$.data.contents").value("updated review contents"))
                .andExpect(jsonPath("$.data.rating").value(4.5))
        ;
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 내용 길이 미달")
    void updateReview_fail_contents_too_short() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("contents", "short") // 10자 미만
                        .param("rating", "4.5")
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
    void updateReview_fail_rating_out_of_range() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("contents", "This is a valid content.")
                        .param("rating", "5.1") // 5 초과
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
    void updateReview_fail_not_review_owner() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));
        given(reviewService.updateReview(any(User.class), any(UpdateReviewDto.Request.class),
                any(Long.class))
        ).willThrow(new RestApiException(NOT_REVIEW_OWNER));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("contents", "This is a valid content.")
                        .param("rating", "4.5")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(NOT_REVIEW_OWNER.name()))
                .andExpect(jsonPath("$.message").value(NOT_REVIEW_OWNER.getDescription()))
        ;
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재 하지 않는 리뷰 ID")
    void updateReview_fail_not_exist_review_id() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));
        given(reviewService.updateReview(any(User.class), any(UpdateReviewDto.Request.class),
                any(Long.class))
        ).willThrow(new RestApiException(REVIEW_NOT_FOUND));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart(HttpMethod.PUT, "/api/reviews/{reviewId}", 1L)
                        .file("image1", image1.getBytes())
                        .file("image2", image2.getBytes())
                        .file("image3", image3.getBytes())
                        .param("contents", "This is a valid content.")
                        .param("rating", "4.5")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REVIEW_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(REVIEW_NOT_FOUND.getDescription()))
        ;
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_success() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));
        given(reviewService.deleteReview(any(User.class), any(Long.class))
        ).willReturn(new DeleteReviewResponse(1L));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/reviews/{reviewId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(1L))
        ;
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 리뷰 작성자가 아닌 사용자")
    void deleteReview_fail_not_review_owner() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(new User()));
        given(reviewService.deleteReview(any(User.class), any(Long.class))
        ).willThrow(new RestApiException(NOT_REVIEW_OWNER));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/reviews/{reviewId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(NOT_REVIEW_OWNER.name()))
                .andExpect(jsonPath("$.message").value(NOT_REVIEW_OWNER.getDescription()))
        ;
    }

}