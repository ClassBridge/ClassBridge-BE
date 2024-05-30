package com.linked.classbridge.util;

import jakarta.servlet.http.Cookie;

// 쿠키를 생성하는 유틸리티 클래스
public class CookieUtil {

    public static Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24); // 쿠키의 유효시간을 24시간으로 설정
        cookie.setPath("/"); // 쿠키가 해당 도메인의 모든 경로에서 유효하도록 설정
        cookie.setHttpOnly(true); // JavaScript를 통해 쿠키에 접근하지 못하도록 설정 (XSS 방지)
        return cookie;
    }
}
