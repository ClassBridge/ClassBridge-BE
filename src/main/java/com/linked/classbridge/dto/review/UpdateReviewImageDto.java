package com.linked.classbridge.dto.review;

import com.linked.classbridge.type.ImageUpdateAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NotNull
@AllArgsConstructor
public class UpdateReviewImageDto {

    private Long imageId;

    private Integer sequence;

    private ImageUpdateAction action;
}
