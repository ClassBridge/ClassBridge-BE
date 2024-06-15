package com.linked.classbridge.dto.tutor;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorInfoDto {

    private String bank;

    private String account;

    @Pattern(regexp = "^[0-9]{10}$", message = "사업자등록번호는 10자리 숫자입니다")
    private String businessRegistrationNumber;

    private String introduction;
}
