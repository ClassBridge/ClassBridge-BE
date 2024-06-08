package com.linked.classbridge.oauth2;

import static com.linked.classbridge.util.CookieUtil.createCookie;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

// OAuth2 로그인 성공 시 JWT 토큰을 생성하고 쿠키에 저장하는 핸들러
// 만약, 해당 이메일로 가입된 유저가 존재하지 않는다면, CustomOAuth2User를 세션에 저장하고 추가 정보 입력 페이지로 리다이렉트
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JWTService jwtService;

    public CustomSuccessHandler(UserRepository userRepository, JWTService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = customOAuth2User.getEmail();

        boolean userExists = userRepository.existsByEmail(email);
        if (userExists) {
            User user = userRepository.findByEmail(email).get();
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.name().substring(5))
                    .collect(Collectors.toList());

            //토큰 생성
            String access = jwtService.createJwt("access", email, roles, 600000L);
            String refresh = jwtService.createJwt("refresh", email, roles, 86400000L);

            //응답 설정
            response.setHeader("Authorization", "Bearer " + access);
            response.addCookie(createCookie("refresh", refresh));
            response.setStatus(HttpStatus.OK.value());

            response.sendRedirect("http://localhost:3000/redirect?type=login&newUser=false");
        } else {
            HttpSession session = request.getSession();
            session.setAttribute("customOAuth2User", customOAuth2User);
            response.sendRedirect("http://localhost:3000/redirect?type=login&newUser=true");
        }
    }
}
