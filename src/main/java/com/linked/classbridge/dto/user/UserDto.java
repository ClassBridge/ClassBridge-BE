package com.linked.classbridge.dto.user;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.type.AuthType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String provider;
    private String providerId;
    private String email;
    private String password;
    private String username;
    private AuthType authType;
    private List<String> roles;

    public static UserDto from(User user) {
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}