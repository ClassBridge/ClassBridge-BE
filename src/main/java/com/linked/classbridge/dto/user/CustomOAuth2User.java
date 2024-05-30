package com.linked.classbridge.dto.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

    private final UserDto userDTO;

    public CustomOAuth2User(UserDto userDTO) {

        this.userDTO = userDTO;
    }

    // 사용자의 속성을 반환
    @Override
    public Map<String, Object> getAttributes() {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("provider", userDTO.getProvider());
        attributes.put("providerId", userDTO.getProviderId());
        attributes.put("email", userDTO.getEmail());
        attributes.put("username", userDTO.getUsername());
        attributes.put("authType", userDTO.getAuthType());
        attributes.put("roles", userDTO.getRoles());
        return attributes;
    }

    // 사용자의 권한을 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : userDTO.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    @Override
    public String getName() {

        return userDTO.getUsername();
    }

    public String getEmail() {

        return userDTO.getEmail();
    }

}
