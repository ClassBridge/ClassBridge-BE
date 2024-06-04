package com.linked.classbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.classbridge.dto.user.AdditionalInfoDto;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.service.UserService;
import com.linked.classbridge.type.AuthType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    private AuthDto.SignUp signupRequest;
    private AuthDto.SignIn signinRequest;

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
        additionalInfoDto.setInterests("Coding");
        additionalInfoDto.setProfileImage("http://example.com/profile.jpg");

        signupRequest = new AuthDto.SignUp();
        signupRequest.setUserDto(userDto);
        signupRequest.setAdditionalInfoDto(additionalInfoDto);

        signinRequest = new AuthDto.SignIn();
        signinRequest.setEmail("testEmail@test.com");
        signinRequest.setPassword("testPassword");
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

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입(이메일 회원가입) 실패 - 필수 정보 누락/닉네임")
    @WithMockUser(username = "test")
    void signup_failure_nickname_null() throws Exception {

        signupRequest.getAdditionalInfoDto().setNickname(null);
        doThrow(new RestApiException(REQUIRED_USER_INFO)).when(userService).addUser(signupRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입(이메일 회원가입) 실패 - 필수 정보 누락/전화번호")
    @WithMockUser(username = "test")
    void signup_failure_phone_number_null() throws Exception {

        signupRequest.getAdditionalInfoDto().setPhoneNumber(null);
        doThrow(new RestApiException(REQUIRED_USER_INFO)).when(userService).addUser(signupRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입(이메일 회원가입) 실패 - 필수 정보 누락/userDto null")
    @WithMockUser(username = "test")
    void signup_failure_email_auth_type_userDto_null() throws Exception {

        signupRequest.setUserDto(null);
        doThrow(new RestApiException(REQUIRED_USER_INFO)).when(userService).addUser(signupRequest);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
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
}