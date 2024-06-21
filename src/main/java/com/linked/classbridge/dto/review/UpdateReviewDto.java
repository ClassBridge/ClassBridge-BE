package com.linked.classbridge.dto.review;

import com.linked.classbridge.domain.Lesson;
import com.linked.classbridge.domain.OneDayClass;
import com.linked.classbridge.domain.Review;
import com.linked.classbridge.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UpdateReviewDto {

    public record Request(
            @Schema(description = "리뷰 내용", example = "유익한 수업이었습니다.")
            @NotBlank(message = "리뷰 내용을 입력해 주세요.")
            @Size(min = 10, max = 200, message = "리뷰 내용은 10자 이상 200자 이하로 입력해 주세요.")
            String contents,

            @Schema(description = "평점", example = "4.5", minimum = "0", maximum = "5")
            @Min(value = 0, message = "평점은 0 이상 5 이하로 입력해 주세요.")
            @Max(value = 5, message = "평점은 0 이상 5 이하로 입력해 주세요.")
            @NotNull(message = "평점을 입력해 주세요.")
            Double rating,

            List<UpdateReviewImageDto> updateReviewImageRequest
    ) {

        public static Review toEntity(
                User user, Lesson lesson, OneDayClass oneDayClass, RegisterReviewDto.Request request
        ) {
            return Review.builder()
                    .user(user)
                    .lesson(lesson)
                    .oneDayClass(oneDayClass)
                    .contents(request.contents())
                    .rating(request.rating())
                    .build();
        }
    }

    public record Response(
            Long reviewId,
            String contents,
            Double rating

    ) {
        public static UpdateReviewDto.Response fromEntity(Review review) {
            return new UpdateReviewDto.Response(
                    review.getReviewId(),
                    review.getContents(),
                    review.getRating()
            );
        }
    }
}
