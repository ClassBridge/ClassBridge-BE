package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.REQUIRED_USER_INFO;
import static com.linked.classbridge.type.ErrorCode.SESSION_DOES_NOT_CONTAIN_CUSTOM_OAUTH2_USER;
import static org.springframework.http.HttpStatus.OK;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        UserDto userDto = signupRequest.getUserDto();

        if (userDto == null) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) session.getAttribute("customOAuth2User");
            if (customOAuth2User == null) {
                throw new RestApiException(SESSION_DOES_NOT_CONTAIN_CUSTOM_OAUTH2_USER);
            }
            userDto = userService.getUserDto(customOAuth2User);
        }

        if (userDto != null && userDto.getAuthType() == AuthType.GOOGLE) {
            signupRequest.setUserDto(userDto);

            if(signupRequest.getAdditionalInfoDto().getPhoneNumber() == null
                    || signupRequest.getAdditionalInfoDto().getPhoneNumber().isEmpty()) {
                throw new RestApiException(REQUIRED_USER_INFO);
            }

            if(signupRequest.getAdditionalInfoDto().getNickname() == null
                    || signupRequest.getAdditionalInfoDto().getNickname().isEmpty()) {
                throw new RestApiException(REQUIRED_USER_INFO);
            }

            userService.addUser(signupRequest);
            session.removeAttribute("customOAuth2User"); // 회원가입 후 세션에서 유저 기본 정보 제거
            return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of("success"));
        } else if (userDto != null && userDto.getAuthType() == AuthType.EMAIL) {

            if(signupRequest.getAdditionalInfoDto().getPhoneNumber() == null
                    || signupRequest.getAdditionalInfoDto().getPhoneNumber().isEmpty()) {
                throw new RestApiException(REQUIRED_USER_INFO);
            }

            if(signupRequest.getAdditionalInfoDto().getNickname() == null
                    || signupRequest.getAdditionalInfoDto().getNickname().isEmpty()) {
                throw new RestApiException(REQUIRED_USER_INFO);
            }

            // 일반 회원가입 방식 처리
            userService.addUser(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of("success"));
        } else {
            throw new RestApiException(REQUIRED_USER_INFO);
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
