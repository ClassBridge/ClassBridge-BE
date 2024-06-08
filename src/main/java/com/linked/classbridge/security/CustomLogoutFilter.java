package com.linked.classbridge.security;

import com.linked.classbridge.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.minidev.json.JSONObject;
import org.springframework.web.filter.GenericFilterBean;

public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTService jwtService;

    public CustomLogoutFilter(JWTService jwtService) {
        this.jwtService = jwtService; // JWT 유틸리티 객체 초기화
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain); // 필터 체인을 통해 요청과 응답을 처리
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // 요청 URI와 메소드 검증
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/api\\/users\\/auth\\/logout$")) { // 요청 URI가 /api/users/auth/logout이 아니면 필터 체인을 계속 진행
            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) { // 요청 메소드가 POST가 아니면 필터 체인을 계속 진행
            filterChain.doFilter(request, response);
            return;
        }

        // refresh 토큰 가져오기
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) { // 쿠키에서 refresh 토큰 찾기
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        // refresh 토큰 null 체크
        if (refresh == null) { // refresh 토큰이 없으면 400 에러 반환
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 만료 체크
        if(jwtService.isExpired(refresh)) { // refresh 토큰이 만료되었으면 400 에러 반환
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String tokenType = jwtService.getTokenType(refresh);
        if (!tokenType.equals("refresh")) { // 토큰 카테고리가 refresh가 아니면 400 에러 반환
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 로그아웃 진행
        // Refresh 토큰 Cookie 값 0으로 설정
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0); // 쿠키의 최대 나이를 0으로 설정하여 즉시 만료
        cookie.setPath("/"); // 쿠키의 경로 설정

        response.addCookie(cookie); // 응답에 쿠키 추가
        response.setStatus(HttpServletResponse.SC_OK); // 200 상태 코드 설정
        JSONObject json = new JSONObject();
        json.put("message", "success");
        response.getWriter().write(json.toString());
    }
}
