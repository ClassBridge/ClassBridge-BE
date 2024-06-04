package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.CLASS_NOT_FOUND;
import static com.linked.classbridge.type.ResponseMessage.REVIEW_GET_SUCCESS;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.ReviewService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OneDayClassController.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class OneDayClassControllerTest {

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    private Pageable pageable;
    private GetReviewResponse reviewResponse1;
    private GetReviewResponse reviewResponse2;

    @BeforeEach
    void setUp() {
        // 0페이지, 5개, 내림차순, createdAt 기준
        pageable = PageRequest.of(0, 5, Direction.DESC, "createdAt");
        reviewResponse1 = new GetReviewResponse(
                1L, 1L, "className", 1L, 1L,
                "userNickname1", 4.5, "content1",
                LocalDate.of(2024, 6, 1),
                LocalDateTime.of(2024, 6, 3, 15, 0),
                List.of("url1", "url2", "url3")
        );
        reviewResponse2 = new GetReviewResponse(
                2L, 1L, "className", 2L, 2L,
                "userNickname2", 2.3, "content2",
                LocalDate.of(2024, 6, 1),
                LocalDateTime.of(2024, 6, 2, 17, 0),
                List.of("url4", "url5", "url6")
        );
    }

    @Test
    @DisplayName("클래스 리뷰 목록 조회 성공")
    void getClassReviews() throws Exception {
        // given
        given(reviewService.getClassReviews(1L, pageable)).willReturn(
                new PageImpl<>(Arrays.asList(reviewResponse1, reviewResponse2), pageable, 2));

        // when & then
        mockMvc.perform(get("/api/class/{classId}/reviews", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value(REVIEW_GET_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.content[0].reviewId").value(reviewResponse1.reviewId()))
                .andExpect(jsonPath("$.data.content[0].classId").value(reviewResponse1.classId()))
                .andExpect(jsonPath("$.data.content[0].userId").value(reviewResponse1.userId()))
                .andExpect(jsonPath("$.data.content[0].rating").value(reviewResponse1.rating()))
                .andExpect(jsonPath("$.data.content[0].contents").value(reviewResponse1.contents()))
                .andExpect(jsonPath("$.data.content[1].reviewId").value(reviewResponse2.reviewId()))
                .andExpect(jsonPath("$.data.content[1].classId").value(reviewResponse2.classId()))
                .andExpect(jsonPath("$.data.content[1].userId").value(reviewResponse2.userId()))
                .andExpect(jsonPath("$.data.content[1].rating").value(reviewResponse2.rating()))
                .andExpect(jsonPath("$.data.content[1].contents").value(reviewResponse2.contents()))
        ;
    }

    @Test
    @DisplayName("클래스 리뷰 목록 조회 실패 - 존재하지 않는 클래스")
    void getClassReviewsFail() throws Exception {
        // given
        given(reviewService.getClassReviews(1L, pageable)).willThrow(
                new RestApiException(CLASS_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/class/{classId}/reviews", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CLASS_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(CLASS_NOT_FOUND.getDescription()))
        ;
    }


}