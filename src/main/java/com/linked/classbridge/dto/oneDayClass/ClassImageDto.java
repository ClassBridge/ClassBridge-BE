package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.ClassImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassImageDto {
    private Long classImageId;
    private String name;
    private String url;
    private int sequence;

    public ClassImageDto(ClassImage classImage) {
        this.classImageId = classImage.getClassImageId();
        this.name = classImage.getName();
        this.url = classImage.getUrl();
        this.sequence = classImage.getSequence();
    }
}
