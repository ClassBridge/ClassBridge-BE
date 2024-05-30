package com.linked.classbridge.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/auth/check-nickname")
    public ResponseEntity<SuccessResponse<?>> checkNickname(@RequestParam String nickname) {

        return ResponseEntity.ok(
                SuccessResponse.of(userService.checkNickname(nickname))
        );
    }

    @GetMapping("/auth/check-email")
    public ResponseEntity<SuccessResponse<?>> checkEmail(@RequestParam String email) {

        return ResponseEntity.ok(
                SuccessResponse.of(userService.checkEmail(email))
        );
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<SuccessResponse<?>> signup(@RequestBody AuthDto.SignUp signupRequest, HttpSession session) {

        UserDto userDto = signupRequest.getUserDTO();

        if (userDto == null) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) session.getAttribute("customOAuth2User");
            if (customOAuth2User == null) {
                throw new IllegalArgumentException("Session does not contain CustomOAuth2User");
            }
            userDto = userService.getUserDto(customOAuth2User);
        }

        if (userDto != null && userDto.getAuthType() == AuthType.GOOGLE) {
            signupRequest.setUserDTO(userDto);
            userService.addUser(signupRequest);
            return ResponseEntity.status(CREATED).body(
                    SuccessResponse.of("success")
            );
        } else {
            throw new IllegalArgumentException("User information is not found from Google API");
        }
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<SuccessResponse<?>> signin(@RequestBody AuthDto.SignIn signinRequest) {

        userService.signin(signinRequest);
        return ResponseEntity.status(OK).body(
                SuccessResponse.of("success")
        );
    }
}
