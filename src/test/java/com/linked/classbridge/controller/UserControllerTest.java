package com.linked.classbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.dto.user.AdditionalInfoDto;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.linked.classbridge.type.ErrorCode.ALREADY_EXIST_NICKNAME;
import static com.linked.classbridge.type.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.linked.classbridge.type.ErrorCode.PASSWORD_NOT_MATCH;
import static com.linked.classbridge.type.ErrorCode.REQUIRED_USER_INFO;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.review.GetReviewResponse;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.service.ReviewService;
import com.linked.classbridge.type.ResponseMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.TestPropertySource;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
class UserControllerTest {

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    private AuthDto.SignUp signupRequest;
    private AuthDto.SignIn signinRequest;

    private Pageable pageable;

    private User mockUser;
    private GetReviewResponse reviewResponse1;
    private GetReviewResponse reviewResponse2;

    private GetReviewResponse reviewResponse3;
    private GetReviewResponse reviewResponse4;

    @BeforeEach
    void setUp() {

        // given
        UserDto userDto = new UserDto();
        userDto.setProvider("testProvider");
        userDto.setProviderId("testProviderId");
        userDto.setEmail("testEmail@test.com");
        userDto.setPassword("testPassword");
        userDto.setUsername("testUsername");
        userDto.setAuthType(AuthType.EMAIL);
        userDto.setRoles(List.of("USER"));

        AdditionalInfoDto additionalInfoDto = new AdditionalInfoDto();
        additionalInfoDto.setNickname("testNickname");
        additionalInfoDto.setPhoneNumber("010-1234-5678");
        additionalInfoDto.setGender("MALE");
        additionalInfoDto.setBirthDate("2001-01-01");
        additionalInfoDto.setInterests(Collections.singletonList("COOKING"));

        signupRequest = new AuthDto.SignUp();
        signupRequest.setUserDto(userDto);
        signupRequest.setAdditionalInfoDto(additionalInfoDto);

        signinRequest = new AuthDto.SignIn();
        signinRequest.setEmail("testEmail@test.com");
        signinRequest.setPassword("testPassword");

        mockUser = User.builder()
                .userId(1L)
                .nickname("userNickname")
                .build();

        pageable = PageRequest.of(0, 5, Direction.DESC, "createdAt");

        reviewResponse1 = new GetReviewResponse(
                1L, 1L, "className", 1L, 1L,
                "userNickname", 4.5, "review1 content",
                LocalDate.of(2024, 6, 1),
                LocalDateTime.of(2024, 6, 3, 15, 0),
                List.of("url1", "url2", "url3")
        );
        reviewResponse2 = new GetReviewResponse(
                2L, 1L, "className", 2L, 1L,
                "userNickname", 2.3, "review2 content",
                LocalDate.of(2024, 6, 1),
                LocalDateTime.of(2024, 6, 2, 17, 0),
                List.of("url4", "url5", "url6")
        );
        reviewResponse3 = new GetReviewResponse(
                3L, 2L, "className", 3L, 1L,
                "userNickname", 3.7, "review3 content",
                LocalDate.of(2024, 6, 1),
                LocalDateTime.of(2024, 6, 1, 20, 0),
                List.of("url7", "url8", "url9")
        );
        reviewResponse4 = new GetReviewResponse(
                4L, 2L, "className", 4L, 1L,
                "userNickname", 5.0, "review4 content",
                LocalDate.of(2024, 6, 1),
                LocalDateTime.of(2024, 6, 1, 23, 0),
                List.of("url10", "url11", "url12")
        );
    }

    @Test
    @DisplayName("닉네임 중복 확인 성공")
    @WithMockUser(username = "test")
    void checkNickname_success() throws Exception {
        when(userService.checkNickname("Nickname")).thenReturn("Use this nickname");

        mockMvc.perform(get("/api/users/auth/check-nickname")
                        .param("nickname", "Nickname"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Use this nickname"));
    }

    @Test
    @DisplayName("닉네임 중복 확인 실패 - 이미 존재하는 닉네임")
    @WithMockUser(username = "test")
    void checkNickname_failure() throws Exception {
        when(userService.checkNickname("testNickname")).thenThrow(new RestApiException(ALREADY_EXIST_NICKNAME));

        mockMvc.perform(get("/api/users/auth/check-nickname")
                        .param("nickname", "testNickname"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 중복 확인 성공")
    @WithMockUser(username = "test")
    void checkEmail_success() throws Exception {
        when(userService.checkEmail("test@test.com")).thenReturn("Use this email");

        mockMvc.perform(get("/api/users/auth/check-email")
                        .param("email", "test@test.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Use this email"));
    }

    @Test
    @DisplayName("이메일 중복 확인 실패 - 이미 존재하는 이메일")
    @WithMockUser(username = "test")
    void checkEmail_failure() throws Exception {
        when(userService.checkEmail("testEmail@test.com")).thenThrow(new RestApiException(ALREADY_REGISTERED_EMAIL));

        mockMvc.perform(get("/api/users/auth/check-email")
                        .param("email", "testEmail@test.com"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입(이메일 회원가입) 성공")
    @WithMockUser(username = "test")
    void signup_success() throws Exception {
        doNothing().when(userService).addUser(signupRequest);

        MockMultipartFile signupRequestPart =
                new MockMultipartFile("signupRequest", "", "application/json", new ObjectMapper().writeValueAsBytes(signupRequest));

        MockMultipartFile profileImagePart =
                new MockMultipartFile("profileImage", "profileImage.png", "image/png", new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/users/auth/signup")
                        .file(signupRequestPart)
                        .file(profileImagePart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입(이메일 회원가입) 실패 - 필수 정보 누락/닉네임")
    @WithMockUser(username = "test")
    void signup_failure_nickname_null() throws Exception {
        signupRequest.getAdditionalInfoDto().setNickname(null);
        doThrow(new RestApiException(REQUIRED_USER_INFO)).when(userService).addUser(signupRequest);

        MockMultipartFile signupRequestPart =
                new MockMultipartFile("signupRequest", "", "application/json", new ObjectMapper().writeValueAsBytes(signupRequest));

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/users/auth/signup")
                        .file(signupRequestPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입(이메일 회원가입) 실패 - 필수 정보 누락/전화번호")
    @WithMockUser(username = "test")
    void signup_failure_phone_number_null() throws Exception {
        signupRequest.getAdditionalInfoDto().setPhoneNumber(null);
        doThrow(new RestApiException(REQUIRED_USER_INFO)).when(userService).addUser(signupRequest);

        MockMultipartFile signupRequestPart =
                new MockMultipartFile("signupRequest", "", "application/json", new ObjectMapper().writeValueAsBytes(signupRequest));

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/users/auth/signup")
                        .file(signupRequestPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입(이메일 회원가입) 실패 - 필수 정보 누락/userDto null")
    @WithMockUser(username = "test")
    void signup_failure_email_auth_type_userDto_null() throws Exception {
        signupRequest.setUserDto(null);
        doThrow(new RestApiException(REQUIRED_USER_INFO)).when(userService).addUser(signupRequest);

        MockMultipartFile signupRequestPart =
                new MockMultipartFile("signupRequest", "", "application/json", new ObjectMapper().writeValueAsBytes(signupRequest));

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/users/auth/signup")
                        .file(signupRequestPart)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인(이메일과 비밀번호로 로그인) 성공")
    @WithMockUser(username = "test")
    void signin_success() throws Exception {

        doNothing().when(userService).signin(signinRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signinRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인(이메일과 비밀번호로 로그인) 실패 - 등록되지 않은 이메일")
    @WithMockUser(username = "test")
    void signin_failure_user_not_found() throws Exception {

        doThrow(new RestApiException(USER_NOT_FOUND)).when(userService).signin(signinRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signinRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("로그인(이메일과 비밀번호로 로그인) 실패 - 비밀번호 불일치")
    @WithMockUser(username = "test")
    void signin_failure_password_not_match() throws Exception {

        doThrow(new RestApiException(PASSWORD_NOT_MATCH)).when(userService).signin(signinRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signinRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 정보 수정 성공")
    @WithMockUser(username = "test", roles = {"ROLE_USER", "ROLE_TUTOR"})
    public void updateUser_success() throws Exception {

        // given
        AdditionalInfoDto additionalInfoDto = new AdditionalInfoDto();
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profileImage.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "profileImage".getBytes()
        );

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/update")
                        .file(profileImage)
                        .param("additionalInfo", new ObjectMapper().writeValueAsString(additionalInfoDto))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 리뷰 목록 조회")
    void getUserReviews() throws Exception {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(reviewService.getUserReviews(mockUser, pageable)).willReturn(new PageImpl<>(
                Arrays.asList(reviewResponse1, reviewResponse2, reviewResponse3, reviewResponse4)
                , pageable, 4)
        );

        // when & then
        mockMvc.perform(get("/api/users/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value(
                        ResponseMessage.REVIEW_GET_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.content[0].reviewId").value(reviewResponse1.reviewId()))
                .andExpect(jsonPath("$.data.content[0].classId").value(reviewResponse1.classId()))
                .andExpect(jsonPath("$.data.content[0].lessonId").value(reviewResponse1.lessonId()))
                .andExpect(jsonPath("$.data.content[0].userId").value(reviewResponse1.userId()))
                .andExpect(jsonPath("$.data.content[1].reviewId").value(reviewResponse2.reviewId()))
                .andExpect(jsonPath("$.data.content[1].classId").value(reviewResponse2.classId()))
                .andExpect(jsonPath("$.data.content[1].lessonId").value(reviewResponse2.lessonId()))
                .andExpect(jsonPath("$.data.content[1].userId").value(reviewResponse2.userId()))
                .andExpect(jsonPath("$.data.content[2].reviewId").value(reviewResponse3.reviewId()))
                .andExpect(jsonPath("$.data.content[2].classId").value(reviewResponse3.classId()))
                .andExpect(jsonPath("$.data.content[2].lessonId").value(reviewResponse3.lessonId()))
                .andExpect(jsonPath("$.data.content[2].userId").value(reviewResponse3.userId()))
                .andExpect(jsonPath("$.data.content[3].reviewId").value(reviewResponse4.reviewId()))
                .andExpect(jsonPath("$.data.content[3].classId").value(reviewResponse4.classId()))
                .andExpect(jsonPath("$.data.content[3].lessonId").value(reviewResponse4.lessonId()))
                .andExpect(jsonPath("$.data.content[3].userId").value(reviewResponse4.userId()));
    }
}