package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.type.ImageUpdateAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NotNull
@AllArgsConstructor
public class UpdateClassImageDto {
    private Long imageId;

    private Integer sequence;

    private ImageUpdateAction action;
}
