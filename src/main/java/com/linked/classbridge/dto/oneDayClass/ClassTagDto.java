package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.ClassTag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClassTagDto {
    private Long tagId;
    private String name;

    public ClassTagDto(ClassTag tag) {
        tagId = tag.getTagId();
        name = tag.getName();
    }
}
