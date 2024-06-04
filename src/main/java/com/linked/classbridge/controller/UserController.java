package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.REQUIRED_USER_INFO;
import static com.linked.classbridge.type.ErrorCode.SESSION_DOES_NOT_CONTAIN_CUSTOM_OAUTH2_USER;
import static org.springframework.http.HttpStatus.OK;

import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.user.AdditionalInfoDto;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.ResponseMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/auth/check-nickname")
    public ResponseEntity<SuccessResponse<String>> checkNickname(@RequestParam String nickname) {

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.NO_MATCHED_NICKNAME,
                        userService.checkNickname(nickname)
                )
        );
    }

    @GetMapping("/auth/check-email")
    public ResponseEntity<SuccessResponse<String>> checkEmail(@RequestParam String email) {

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.NO_MATCHED_EMAIL,
                        userService.checkEmail(email)
                )
        );
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<SuccessResponse<String>> signup(
            @RequestPart("signupRequest") AuthDto.SignUp signupRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpSession session
    ) {

        UserDto userDto = signupRequest.getUserDto();

        if (userDto == null) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) session.getAttribute("customOAuth2User");
            if (customOAuth2User == null) {
                throw new RestApiException(SESSION_DOES_NOT_CONTAIN_CUSTOM_OAUTH2_USER);
            }
            userDto = userService.getUserDto(customOAuth2User);
        }

        AdditionalInfoDto additionalInfoDto = signupRequest.getAdditionalInfoDto();

        if (additionalInfoDto == null) {
            throw new RestApiException(REQUIRED_USER_INFO);
        } else {
            if(profileImage != null) {
                additionalInfoDto.setProfileImage(profileImage);
            }

            if(additionalInfoDto.getPhoneNumber() == null
                    || signupRequest.getAdditionalInfoDto().getPhoneNumber().isEmpty()) {
                throw new RestApiException(REQUIRED_USER_INFO);
            }

            if(signupRequest.getAdditionalInfoDto().getNickname() == null
                    || signupRequest.getAdditionalInfoDto().getNickname().isEmpty()) {
                throw new RestApiException(REQUIRED_USER_INFO);
            }
        }

        signupRequest.setUserDto(userDto);
        signupRequest.setAdditionalInfoDto(additionalInfoDto);

        if (userDto != null && userDto.getAuthType() == AuthType.GOOGLE) {

            // 구글 회원가입 방식 처리
            userService.addUser(signupRequest);
            session.removeAttribute("customOAuth2User"); // 회원가입 후 세션에서 유저 기본 정보 제거
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    SuccessResponse.of(
                            ResponseMessage.SIGNUP_SUCCESS,
                            "success"
                    )
            );
        } else if (userDto != null && userDto.getAuthType() == AuthType.EMAIL) {

            // 일반 회원가입 방식 처리
            userService.addUser(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    SuccessResponse.of(
                            ResponseMessage.SIGNUP_SUCCESS,
                            "success"
                    )
            );
        } else {
            throw new RestApiException(REQUIRED_USER_INFO);
        }
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<SuccessResponse<String>> signin(@RequestBody AuthDto.SignIn signinRequest) {

        userService.signin(signinRequest);
        return ResponseEntity.status(OK).body(
                SuccessResponse.of(
                        ResponseMessage.LOGIN_SUCCESS,
                        "success"
                )
        );
    }
}
