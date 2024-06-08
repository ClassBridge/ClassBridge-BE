package com.linked.classbridge.controller;

import static com.linked.classbridge.util.CookieUtil.createCookie;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.type.ErrorCode;
import com.linked.classbridge.type.ResponseMessage;
import com.linked.classbridge.service.JWTService;
import com.linked.classbridge.type.TokenType;
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

    private final JWTService jwtService;

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

        if(jwtService.isExpired(refresh)) {
            throw new RestApiException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String tokenType = jwtService.getTokenType(refresh);
        if (!tokenType.equals("refresh")) {
            throw new RestApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtService.getEmail(refresh);
        List<String> roles = jwtService.getRoles(refresh);

        String newAccess = jwtService.createJwt(TokenType.ACCESS.getValue(), email, roles, TokenType.ACCESS.getExpiryTime());
        String newRefresh = jwtService.createJwt(TokenType.REFRESH.getValue(), email, roles, TokenType.REFRESH.getExpiryTime());

        response.setHeader(TokenType.ACCESS.getValue(), newAccess);
        response.addCookie(createCookie(TokenType.REFRESH.getValue(), newRefresh));

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.ACCESS_TOKEN_ISSUED,
                        "success"
                )
        );
    }
}
