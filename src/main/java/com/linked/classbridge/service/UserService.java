package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.ALREADY_EXIST_NICKNAME;
import static com.linked.classbridge.type.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.linked.classbridge.type.ErrorCode.PASSWORD_NOT_MATCH;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;

import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.user.AdditionalInfoDto;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.GoogleResponse;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.Gender;
import com.linked.classbridge.type.UserRole;
import com.linked.classbridge.util.CookieUtil;
import com.linked.classbridge.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JWTUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String checkNickname(String nickname) {

        if(userRepository.existsByNickname(nickname)){
            throw new RestApiException(ALREADY_EXIST_NICKNAME);
        }
        return "you can use this nickname";
    }

    public String checkEmail(String email) {

        if(userRepository.existsByEmail(email)){
            throw new RestApiException(ALREADY_REGISTERED_EMAIL);
        }
        return "you can use this email";
    }

    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    public UserDto getUserFromGoogle(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v1/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> attributes = response.getBody();
        GoogleResponse googleResponse = new GoogleResponse(attributes);

        UserDto userDto = new UserDto();
        userDto.setProvider("google");
        userDto.setProviderId(googleResponse.getProviderId());
        userDto.setEmail(googleResponse.getEmail());
        userDto.setUsername(googleResponse.getName());
        userDto.setAuthType(AuthType.GOOGLE);

        return userDto;
    }

    public UserDto getUserDto(CustomOAuth2User customOAuth2User) {

        UserDto userDto = new UserDto();
        Map<String, Object> attributes = customOAuth2User.getAttributes();

        userDto.setProvider((String) attributes.get("provider"));
        userDto.setProviderId((String) attributes.get("providerId"));
        userDto.setEmail((String) attributes.get("email"));
        userDto.setUsername((String) attributes.get("username"));
        userDto.setAuthType((AuthType) attributes.get("authType"));

        return userDto;
    }

    public void addUser(AuthDto.SignUp signupRequest) {

        UserDto userDTO = signupRequest.getUserDTO();
        AdditionalInfoDto additionalInfoDTO = signupRequest.getAdditionalInfoDTO();

        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.ROLE_USER);
        Gender gender = additionalInfoDTO.getGender() != null ? Gender.valueOf(additionalInfoDTO.getGender().toUpperCase()) : null;

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RestApiException(ALREADY_REGISTERED_EMAIL);
        }

        User user = User.builder()
                .provider(userDTO.getProvider())
                .providerId(userDTO.getProviderId())
                .email(userDTO.getEmail())
                .username(userDTO.getUsername())
                .authType(userDTO.getAuthType())
                .roles(roles)
                .nickname(additionalInfoDTO.getNickname())
                .phone(additionalInfoDTO.getPhoneNumber())
                .gender(gender)
                .birthDate(additionalInfoDTO.getBirthDate())
                .interests(additionalInfoDTO.getInterests())
                .profileImageUrl(additionalInfoDTO.getProfilePictureUrl())
                .build();

        if(signupRequest.getUserDTO().getPassword() != null) {
            user.setPassword(passwordEncoder.encode(signupRequest.getUserDTO().getPassword()));
        }
        userRepository.save(user);

        // 회원가입 완료 후 JWT 토큰 발급
        String token = jwtUtil.createJwt(userDTO.getEmail(), userDTO.getRoles(), 60 * 60 * 24L * 1000);
        // JWT 토큰을 클라이언트로 전송
        Cookie cookie = CookieUtil.createCookie("Authorization", token);
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        if (response != null) {
            response.addCookie(cookie);
        }
    }

    public void signin(AuthDto.SignIn signinRequest) {

        User user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            throw new RestApiException(PASSWORD_NOT_MATCH);
        }

        List<String> roles = user.getRoles().stream()
                .map(role -> role.name().substring(5)) // "ROLE_" 부분을 제거
                .collect(Collectors.toList());

        String token = jwtUtil.createJwt(user.getEmail(), roles, 60 * 60 * 24L * 1000); // 24 시간
        Cookie cookie = CookieUtil.createCookie("Authorization", token);
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        if (response != null) {
            response.addCookie(cookie);
        }
    }
}
