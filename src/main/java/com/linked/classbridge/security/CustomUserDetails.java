package com.linked.classbridge.security;

import com.linked.classbridge.dto.user.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private UserDto userDto;

    public CustomUserDetails(UserDto userDto) {
        this.userDto = userDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = userDto.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return authorities;
    }

    @Override
    public String getPassword() {
        // 비밀번호는 JWT 토큰에 포함되지 않으므로 null을 반환
        return null;
    }

    @Override
    public String getUsername() {
        return userDto.getEmail(); // 이메일을 사용자 이름으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부는 JWT 토큰에 포함되지 않으므로 true를 반환
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠김 여부는 JWT 토큰에 포함되지 않으므로 true를 반환
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 자격 증명 만료 여부는 JWT 토큰에 포함되지 않으므로 true를 반환
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부는 JWT 토큰에 포함되지 않으므로 true를 반환
        return true;
    }

    public UserDto getUserDto() {
        return userDto;
    }
}
