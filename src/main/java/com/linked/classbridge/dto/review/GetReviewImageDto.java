package com.linked.classbridge.dto.review;

import com.linked.classbridge.domain.ReviewImage;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NotNull
@AllArgsConstructor
public class GetReviewImageDto {

    private Long imageId;
    private Integer sequence;
    private String url;

    public static List<GetReviewImageDto> fromEntityListToDtoList(List<ReviewImage> reviewImageList) {
        return reviewImageList.stream()
                .map(reviewImage -> new GetReviewImageDto(
                        reviewImage.getReviewImageId(),
                        reviewImage.getSequence(),
                        reviewImage.getUrl()
                )).toList();
    }
}
