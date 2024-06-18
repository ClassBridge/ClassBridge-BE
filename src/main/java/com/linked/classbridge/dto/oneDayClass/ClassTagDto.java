package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.ClassTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ClassTagDto {
    private Long tagId;
    private String name;

    public ClassTagDto(ClassTag tag) {
        tagId = tag.getTagId();
        name = tag.getName();
    }
}
