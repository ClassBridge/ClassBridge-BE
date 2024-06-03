package com.linked.classbridge.dto.review;

import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.ReviewImage;
import java.time.LocalDateTime;
import java.util.List;

public record GetReviewResponse(
        Long reviewId,
        Long classId,
        String className,
        Long lessonId,
        Long userId,
        String userNickName,
        Double rating,
        String contents,
        LocalDateTime lessonDate,
        LocalDateTime createdAt,
        List<String> reviewImageUrlList
) {
    public static GetReviewResponse fromEntity(Review review) {

        return new GetReviewResponse(
                review.getReviewId(),
                review.getOneDayClass().getOneDayClassId(),
                review.getOneDayClass().getClassName(),
                review.getLesson().getLessonId(),
                review.getUser().getUserId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContents(),
                review.getLesson().getLessonDate(),
                review.getCreatedAt(),
                review.getReviewImageList().stream()
                        .map(ReviewImage::getUrl)
                        .toList()
        );

    }
}
