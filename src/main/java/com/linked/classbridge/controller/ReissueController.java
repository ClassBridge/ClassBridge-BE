package com.linked.classbridge.controller;

import static com.linked.classbridge.util.CookieUtil.createCookie;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.ResponseMessage;
import com.linked.classbridge.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final JWTUtil jwtUtil;

    @Operation(summary = "토큰 재발급", description = "refresh 토큰을 통해 access 토큰을 재발급")
    @PostMapping("/api/users/auth/reissue")
    public ResponseEntity<SuccessResponse<String>> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            throw new RestApiException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        if(jwtUtil.isExpired(refresh)) {
            throw new RestApiException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new RestApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtUtil.getEmail(refresh);
        List<String> roles = jwtUtil.getRoles(refresh);

        String newAccess = jwtUtil.createJwt("access", email, roles, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", email, roles, 86400000L);

        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.ACCESS_TOKEN_ISSUED,
                        "success"
                )
        );
    }
}
