package com.linked.classbridge.dto.review;

import com.linked.classbridge.domain.Review;
import java.time.LocalDate;
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
        LocalDate lessonDate,
        LocalDateTime createdAt,
        List<GetReviewImageDto> reviewImageList
) {
    public static GetReviewResponse fromEntity(Review review) {

        return new GetReviewResponse(
                review.getReviewId(),
                review.getOneDayClass().getClassId(),
                review.getOneDayClass().getClassName(),
                review.getLesson().getLessonId(),
                review.getUser().getUserId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContents(),
                review.getLesson().getLessonDate(),
                review.getCreatedAt(),
                GetReviewImageDto.fromEntityListToDtoList(review.getReviewImageList())
        );

    }
}
