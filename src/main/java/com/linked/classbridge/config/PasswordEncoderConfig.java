package com.linked.classbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// 비밀번호 인코더에 대한 설정을 담당.
// BCryptPasswordEncoder를 빈으로 등록하여, 애플리케이션 전반에서 사용할 수 있게 함.
// BCryptPasswordEncoder는 비밀번호를 안전하게 인코딩하는 데 사용되며,
// 이는 원시 비밀번호를 저장하는 대신 인코딩된 비밀번호를 저장하여 보안을 강화하는 데 도움이 됨
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}