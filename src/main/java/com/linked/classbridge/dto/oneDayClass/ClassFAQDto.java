package com.linked.classbridge.dto.oneDayClass;

import com.linked.classbridge.domain.ClassFAQ;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClassFAQDto {
    private Long faqId;
    private String title;
    private String content;

    public ClassFAQDto(ClassFAQ classFAQ) {
        this.faqId = classFAQ.getFaqId();
        this.title = classFAQ.getTitle();
        this.content = classFAQ.getContent();
    }

    public ClassFAQ toEntity(ClassFAQDto classFAQDto) {
        return ClassFAQ.builder()
                .faqId(classFAQDto.getFaqId())
                .title(classFAQDto.title)
                .content(classFAQDto.content)
                .build();
    }
}
