package com.linked.classbridge.dto.user;

import com.linked.classbridge.type.AuthType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private String provider;
    private String providerId;
    private String email;
    private String password;
    private String username;
    private AuthType authType;
    private List<String> roles;
}