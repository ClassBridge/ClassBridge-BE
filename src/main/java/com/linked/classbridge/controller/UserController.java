package com.linked.classbridge.controller;

import static com.linked.classbridge.type.ErrorCode.REQUIRED_USER_INFO;
import static com.linked.classbridge.type.ErrorCode.SESSION_DOES_NOT_CONTAIN_CUSTOM_OAUTH2_USER;
import static org.springframework.http.HttpStatus.OK;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.SuccessResponse;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.dto.user.AdditionalInfoDto;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.dto.user.WishDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    private final ReviewService reviewService;

    @Operation(summary = "닉네임 중복 확인", description = "DB에 중복된 닉네임이 있는지 확인")
    @GetMapping("/auth/check-nickname")
    public ResponseEntity<SuccessResponse<String>> checkNickname(@RequestParam String nickname) {

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.NO_MATCHED_NICKNAME,
                        userService.checkNickname(nickname)
                )
        );
    }

    @Operation(summary = "이메일 중복 확인", description = "DB에 중복된 이메일이 있는지 확인")
    @GetMapping("/auth/check-email")
    public ResponseEntity<SuccessResponse<String>> checkEmail(@RequestParam String email) {

        return ResponseEntity.status(HttpStatus.OK).body(
                SuccessResponse.of(
                        ResponseMessage.NO_MATCHED_EMAIL,
                        userService.checkEmail(email)
                )
        );
    }

    @Operation(summary = "회원가입", description = "구글 회원가입 사용자와 일반 회원가입 사용자를 구분하여 회원가입 처리")
    @PostMapping("/auth/signup")
    public ResponseEntity<SuccessResponse<String>> signup(
            @RequestPart("signupRequest") @Valid AuthDto.SignUp signupRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpSession session
    ) {

        UserDto userDto = signupRequest.getUserDto();

        if (userDto == null) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) session.getAttribute("customOAuth2User");
            if (customOAuth2User == null) {
                throw new RestApiException(SESSION_DOES_NOT_CONTAIN_CUSTOM_OAUTH2_USER);
            }
            userDto = userService.getUserDtoFromOAuth2User(customOAuth2User);
        }

        AdditionalInfoDto additionalInfoDto = signupRequest.getAdditionalInfoDto();

        if (additionalInfoDto == null) {
            throw new RestApiException(REQUIRED_USER_INFO);
        } else {
            if (profileImage != null) {
                additionalInfoDto.setProfileImage(profileImage);
            }

            if (additionalInfoDto.getPhoneNumber() == null
                    || signupRequest.getAdditionalInfoDto().getPhoneNumber().isEmpty()) {
                throw new RestApiException(REQUIRED_USER_INFO);
            }

            if (signupRequest.getAdditionalInfoDto().getNickname() == null
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

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인")
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

    @Operation(summary = "유저 정보 수정", description = "유저 정보 수정")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update")
    public ResponseEntity<SuccessResponse<String>> updateUser(
            @RequestPart(value = "additionalInfo", required = false) @Valid AdditionalInfoDto additionalInfoDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        userService.updateUser(additionalInfoDto, profileImage);
        return ResponseEntity.status(OK).body(
                SuccessResponse.of(
                        ResponseMessage.USER_UPDATE_SUCCESS,
                        "success"
                )
        );
    }

    @Operation(summary = "수강생 리뷰 조회", description = "수강생 리뷰 조회")
    @GetMapping("/reviews")
    public ResponseEntity<SuccessResponse<Page<GetReviewResponse>>> getClassReviews(
            @PageableDefault Pageable pageable
    ) {
        User user = userService.getUserByEmail(userService.getCurrentUserEmail());

        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.REVIEW_GET_SUCCESS,
                        reviewService.getUserReviews(user, pageable)
                )
        );
    }

    @Operation(summary = "수강생 찜목록 조회", description = "수강생 찜목록 조회")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/wish")
    public ResponseEntity<SuccessResponse<Page<WishDto>>> getWish(
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.WISH_GET_SUCCESS,
                        userService.getWishList(userService.getCurrentUserEmail(), pageable)
                )
        );
    }

    @Operation(summary = "수강생 찜목록 추가", description = "수강생 찜목록 추가")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/wish")
    public ResponseEntity<SuccessResponse<Boolean>> addWish(
            @RequestBody WishDto.Request request
    ) {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.WISH_ADD_SUCCESS,
                        userService.addWish(userService.getCurrentUserEmail(), request.classId())
                )
        );
    }

    @Operation(summary = "수강생 찜목록 삭제", description = "수강생 찜목록 삭제")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/wish")
    public ResponseEntity<SuccessResponse<Boolean>> deleteWish(
            @RequestBody WishDto.Request request) {
        return ResponseEntity.ok().body(
                SuccessResponse.of(
                        ResponseMessage.WISH_DELETE_SUCCESS,
                        userService.deleteWish(userService.getCurrentUserEmail(), request.classId())
                )
        );
    }
}
