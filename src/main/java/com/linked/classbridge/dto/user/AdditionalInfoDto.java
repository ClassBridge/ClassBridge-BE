package com.linked.classbridge.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdditionalInfoDto {

    private String nickname;
    private String phoneNumber;
    private String gender;  // Optional
    private String birthDate;  // Optional, yyyy.mm.dd
    private String interests;  // Optional, 카테고리 테이블 추가 시 수정
    private String profilePictureUrl;  // Optional
}

