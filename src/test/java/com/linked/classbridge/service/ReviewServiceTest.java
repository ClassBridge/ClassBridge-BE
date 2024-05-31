package com.linked.classbridge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.ReviewImage;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.repository.ReviewImageRepository;
import com.linked.classbridge.repository.ReviewRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private LessonService lessonService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 조회 성공")
    void getReview_success() {
        // given
        Review review = createMockReview();

        given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));

        // when
        GetReviewResponse response = reviewService.getReview(1L);

        // then
        assertNotNull(response);
        assertEquals(response.reviewId(), review.getReviewId());
        assertEquals(response.classId(), review.getOneDayClass().getOneDayClassId());
        assertEquals(response.className(), review.getOneDayClass().getClassName());
        assertEquals(response.lessonId(), review.getLesson().getLessonId());
        assertEquals(response.userId(), review.getUser().getUserId());
        assertEquals(response.userNickName(), review.getUser().getNickname());
        assertEquals(response.rating(), review.getRating());
        assertEquals(response.contents(), review.getContents());
        assertEquals(response.lessonDate(), review.getLesson().getLessonDate());
        assertEquals(response.createdAt(), review.getCreatedAt());
        assertThat(response.reviewImageUrlList()).containsExactly("url1", "url2", "url3");
    }


    private Review createMockReview() {
        OneDayClass oneDayClass = OneDayClass.builder()
                .oneDayClassId(1L)
                .className("oneDayClassName")
                .build();

        Lesson lesson = Lesson.builder()
                .lessonId(1L)
                .oneDayClass(oneDayClass)
                .lessonDate(LocalDateTime.now())
                .build();

        User user = User.builder()
                .userId(1L)
                .nickname("user")
                .build();

        Review review = Review.builder()
                .reviewId(1L)
                .oneDayClass(oneDayClass)
                .lesson(lesson)
                .user(user)
                .rating(5.0)
                .contents("contents")
                .createdAt(LocalDateTime.now())
                .reviewImageList(new ArrayList<>())
                .build();

        ReviewImage image1 = ReviewImage.builder()
                .review(review)
                .sequence(1)
                .url("url1")
                .build();
        review.addReviewImage(image1);

        ReviewImage image2 = ReviewImage.builder()
                .review(review)
                .sequence(2)
                .url("url2")
                .build();
        review.addReviewImage(image2);

        ReviewImage image3 = ReviewImage.builder()
                .review(review)
                .sequence(3)
                .url("url3")
                .build();
        review.addReviewImage(image3);

        return review;
    }
}