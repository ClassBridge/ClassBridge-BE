package com.linked.classbridge.config;

import com.linked.classbridge.oauth2.CustomSuccessHandler;
import com.linked.classbridge.security.CustomAccessDeniedHandler;
import com.linked.classbridge.security.CustomAuthenticationEntryPoint;
import com.linked.classbridge.security.CustomLogoutFilter;
import com.linked.classbridge.security.JWTFilter;
import com.linked.classbridge.service.CustomOAuth2UserService;
import com.linked.classbridge.service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTService jwtService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler,
                          JWTService jwtService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtService = jwtService;
    }

    // CORS 설정 및 CSRF, FormLogin, HTTP Basic 인증 방식 disable, JWTFilter, oauth2, 경로별 인가 작업, 세션 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(
                                Arrays.asList("https://class-bridge.vercel.app", "https://open-api.kakaopay.com"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                        configuration.setExposedHeaders(Collections.singletonList("access"));

                        return configuration;
                    }
                }));
        //csrf disable
        http
                .csrf((auth) -> auth.disable());
        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());
        //HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());
        //JWTFilter
        http
                .addFilterAfter(new JWTFilter(jwtService), OAuth2LoginAuthenticationFilter.class);
        //oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                        .authorizationEndpoint((authorization) -> authorization
                                .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint((redirection) -> redirection
                                .baseUri("/api/users/auth/google")
                        )
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );
        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/swagger-ui/*", "/v3/api-docs/**", "/api/payments/complete").permitAll()
                        .requestMatchers("/", "/api/users/auth/**").permitAll()
                        .requestMatchers("/api/users/auth/reissue").permitAll()
                        .requestMatchers("/api/tutors/**").permitAll()
                        .requestMatchers("/api/class/**").permitAll()
                        .requestMatchers("/CB-websocket/**").permitAll()
                        .requestMatchers("/api/users/badges/**").permitAll()
                        .requestMatchers("/api/openapi/**").permitAll()
                        .requestMatchers("/api/class/recommend/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers("/api/users").hasRole("USER")
                        .requestMatchers("/api/users/**").hasRole("USER")
                        .anyRequest().authenticated())
                .exceptionHandling((exception) -> exception
                        // 인증되지 않은 사용자가 보호된 리소스에 액세스하려고 할 때 호출
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        // 인증은 되었지만, 해당 리소스에 접근할 권한이 없을 때 호출
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                );
        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 로그아웃 설정
        http
                .addFilterBefore(new CustomLogoutFilter(jwtService), LogoutFilter.class);

        return http.build();
    }
}
