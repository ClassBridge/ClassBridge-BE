package com.linked.classbridge.dto.user;

import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AdditionalInfoDto {

    private String nickname;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Pattern(regexp = "^(MALE|FEMALE)?$", message = "Invalid gender")
    private String gender;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid birth date format")
    private String birthDate;

    private List<String> interests;

    private MultipartFile profileImage;
}

