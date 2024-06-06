package com.linked.classbridge.dto.user;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AdditionalInfoDto {

    private String nickname;
    private String phoneNumber; // 010-1234-5678
    private String gender;  // Optional
    private String birthDate;  // Optional, yyyy-mm-dd
    private List<String> interests;  // Optional
    private MultipartFile profileImage;  // Optional
}

