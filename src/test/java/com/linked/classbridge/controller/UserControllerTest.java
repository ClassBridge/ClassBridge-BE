package com.linked.classbridge.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.type.ResponseMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(UserController.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class UserControllerTest {

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private Pageable pageable;

    private User mockUser;
    private GetReviewResponse reviewResponse1;
    private GetReviewResponse reviewResponse2;

    private GetReviewResponse reviewResponse3;
    private GetReviewResponse reviewResponse4;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1L)
                .nickname("userNickname")
                .build();

        pageable = PageRequest.of(0, 5, Direction.DESC, "createdAt");

        reviewResponse1 = new GetReviewResponse(
                1L, 1L, "className", 1L, 1L,
                "userNickname", 4.5, "review1 content",
                LocalDateTime.of(2024, 6, 1, 9, 0),
                LocalDateTime.of(2024, 6, 3, 15, 0),
                List.of("url1", "url2", "url3")
        );
        reviewResponse2 = new GetReviewResponse(
                2L, 1L, "className", 2L, 1L,
                "userNickname", 2.3, "review2 content",
                LocalDateTime.of(2024, 6, 1, 12, 0),
                LocalDateTime.of(2024, 6, 2, 17, 0),
                List.of("url4", "url5", "url6")
        );
        reviewResponse3 = new GetReviewResponse(
                3L, 2L, "className", 3L, 1L,
                "userNickname", 3.7, "review3 content",
                LocalDateTime.of(2024, 6, 1, 15, 0),
                LocalDateTime.of(2024, 6, 1, 20, 0),
                List.of("url7", "url8", "url9")
        );
        reviewResponse4 = new GetReviewResponse(
                4L, 2L, "className", 4L, 1L,
                "userNickname", 5.0, "review4 content",
                LocalDateTime.of(2024, 6, 1, 18, 0),
                LocalDateTime.of(2024, 6, 1, 23, 0),
                List.of("url10", "url11", "url12")
        );
    }

    @Test
    @DisplayName("유저 리뷰 목록 조회")
    void getUserReviews() throws Exception {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(reviewService.getUserReviews(mockUser, pageable)).willReturn(new SliceImpl<>(
                Arrays.asList(reviewResponse1, reviewResponse2, reviewResponse3, reviewResponse4)
                , pageable, true)
        );

        // when & then
        mockMvc.perform(get("/api/users/reviews")
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
}