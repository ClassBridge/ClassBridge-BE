package com.linked.classbridge.dto.user;

import lombok.Data;

public class AuthDto {

    @Data
    public static class SignIn {

        private String email;
        private String password;
    }

    @Data
    public static class SignUp {

        private UserDto userDto;
        private AdditionalInfoDto additionalInfoDto;
    }
}
