package com.linked.classbridge.service;

import static com.linked.classbridge.type.ErrorCode.ALREADY_EXIST_NICKNAME;
import static com.linked.classbridge.type.ErrorCode.ALREADY_REGISTERED_EMAIL;
import static com.linked.classbridge.type.ErrorCode.NOT_AUTHENTICATED_USER;
import static com.linked.classbridge.type.ErrorCode.NO_INFORMATION_TO_UPDATE;
import static com.linked.classbridge.type.ErrorCode.PASSWORD_NOT_MATCH;
import static com.linked.classbridge.type.ErrorCode.UNEXPECTED_PRINCIPAL_TYPE;
import static com.linked.classbridge.type.ErrorCode.USER_NOT_FOUND;
import static com.linked.classbridge.util.CookieUtil.createCookie;

import com.linked.classbridge.domain.Category;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.dto.user.AdditionalInfoDto;
import com.linked.classbridge.dto.user.AuthDto;
import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.GoogleResponse;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.repository.CategoryRepository;
import com.linked.classbridge.repository.UserRepository;
import com.linked.classbridge.security.CustomUserDetails;
import com.linked.classbridge.type.AuthType;
import com.linked.classbridge.type.CategoryType;
import com.linked.classbridge.type.Gender;
import com.linked.classbridge.type.TokenType;
import com.linked.classbridge.type.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final PasswordEncoder passwordEncoder;

    private final JWTService jwtService;

    private final S3Service s3Service;

    public UserService(UserRepository userRepository, CategoryRepository categoryRepository, PasswordEncoder passwordEncoder,
                       JWTService jwtService, S3Service s3Service) {

        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
    }

    public String checkNickname(String nickname) {

        log.info("Checking if nickname '{}' exists", nickname);

        if(userRepository.existsByNickname(nickname)){
            log.warn("Nickname '{}' already exists", nickname);
            throw new RestApiException(ALREADY_EXIST_NICKNAME);
        }

        log.info("Nickname '{}' is available", nickname);
        return "Use this nickname";
    }

    public String checkEmail(String email) {

        log.info("Checking if email '{}' exists", email);

        if(userRepository.existsByEmail(email)){
            log.warn("Email '{}' is already registered", email);
            throw new RestApiException(ALREADY_REGISTERED_EMAIL);
        }

        log.info("Email '{}' is available", email);
        return "Use this email";
    }

    public Optional<User> findByEmail(String email) {

        log.info("Finding user by email '{}'", email);
        return userRepository.findByEmail(email);
    }

    public UserDto getUserFromGoogle(String accessToken) {

        log.info("Getting user from Google with access token");

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

        log.info("Google user '{}' retrieved successfully", userDto.getUsername());
        return userDto;
    }

    public UserDto getUserDtoFromOAuth2User(CustomOAuth2User customOAuth2User) {

        log.info("Getting user DTO from CustomOAuth2User");

        UserDto userDto = new UserDto();
        Map<String, Object> attributes = customOAuth2User.getAttributes();

        userDto.setProvider((String) attributes.get("provider"));
        userDto.setProviderId((String) attributes.get("providerId"));
        userDto.setEmail((String) attributes.get("email"));
        userDto.setUsername((String) attributes.get("username"));
        userDto.setAuthType((AuthType) attributes.get("authType"));

        log.info("User DTO for '{}' created successfully", userDto.getUsername());
        return userDto;
    }

    public void addUser(AuthDto.SignUp signupRequest) {

        log.info("Adding new user with email '{}'", signupRequest.getUserDto().getEmail());

        UserDto userDto = signupRequest.getUserDto();
        AdditionalInfoDto additionalInfoDto = signupRequest.getAdditionalInfoDto();

        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Email '{}' is already registered", userDto.getEmail());
            throw new RestApiException(ALREADY_REGISTERED_EMAIL);
        }

        if(userRepository.existsByNickname(additionalInfoDto.getNickname())) {
            log.warn("Nickname '{}' already exists", additionalInfoDto.getNickname());
            throw new RestApiException(ALREADY_EXIST_NICKNAME);
        }

        List<UserRole> roles = new ArrayList<>();
        roles.add(UserRole.ROLE_USER);
        Gender gender = additionalInfoDto.getGender() != null ? Gender.valueOf(additionalInfoDto.getGender().toUpperCase()) : null;

        // 관심 카테고리 String -> Category 변환
        List<Category> interests = additionalInfoDto.getInterests().stream()
                .map(interest -> categoryRepository.findByName(CategoryType.valueOf(interest)))
                .collect(Collectors.toList());

        User user = User.builder()
                .provider(userDto.getProvider())
                .providerId(userDto.getProviderId())
                .email(userDto.getEmail())
                .username(userDto.getUsername())
                .authType(userDto.getAuthType())
                .roles(roles)
                .nickname(additionalInfoDto.getNickname())
                .phone(additionalInfoDto.getPhoneNumber())
                .gender(gender)
                .birthDate(additionalInfoDto.getBirthDate())
                .interests(interests)
                .build();

        MultipartFile profileImage = signupRequest.getAdditionalInfoDto().getProfileImage();
        if (profileImage != null) {
            String profileImageUrl = s3Service.uploadUserProfileImage(profileImage);
            user.setProfileImageUrl(profileImageUrl);
        }

        if(signupRequest.getUserDto().getPassword() != null) {
            user.setPassword(passwordEncoder.encode(signupRequest.getUserDto().getPassword()));
        }
        userRepository.save(user);
        log.info("User '{}' added successfully", user.getUsername());

        String access = jwtService.createJwt(TokenType.ACCESS.getValue(), user.getEmail(), userDto.getRoles(), TokenType.ACCESS.getExpiryTime());
        String refresh = jwtService.createJwt(TokenType.REFRESH.getValue(), user.getEmail(), userDto.getRoles(), TokenType.REFRESH.getExpiryTime());
        // JWT 토큰을 클라이언트로 전송
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        if (response != null) {
            response.addCookie(createCookie(TokenType.REFRESH.getValue(), refresh));
            response.setHeader(TokenType.ACCESS.getValue(), access);
            log.info("JWT token added to response for user '{}'", user.getUsername());
        }
    }

    public void signin(AuthDto.SignIn signinRequest) {

        log.info("Signing in user with email '{}'", signinRequest.getEmail());

        User user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("User with email '{}' not found", signinRequest.getEmail());
                    return new RestApiException(USER_NOT_FOUND);
                });

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            log.warn("Password does not match for user '{}'", signinRequest.getEmail());
            throw new RestApiException(PASSWORD_NOT_MATCH);
        }

        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        String access = jwtService.createJwt(TokenType.ACCESS.getValue(), user.getEmail(), roles, TokenType.ACCESS.getExpiryTime());
        String refresh = jwtService.createJwt(TokenType.REFRESH.getValue(), user.getEmail(), roles, TokenType.REFRESH.getExpiryTime());
        // JWT 토큰을 클라이언트로 전송
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        if (response != null) {
            response.addCookie(createCookie(TokenType.REFRESH.getValue(), refresh));
            response.setHeader(TokenType.ACCESS.getValue(), access);
            log.info("JWT token added to response for user '{}'", user.getUsername());
        }
    }

    @Transactional
    public void updateUser(AdditionalInfoDto additionalInfoDto, MultipartFile profileImage) {

        log.info("Updating user information");

        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User with email '{}' not found", email);
                    return new RestApiException(USER_NOT_FOUND);
                });

        if(additionalInfoDto == null && profileImage == null) {
            log.warn("No information to update");
            throw new RestApiException(NO_INFORMATION_TO_UPDATE);
        }

        if (additionalInfoDto != null) {
            if (additionalInfoDto.getNickname() != null) {
                if (additionalInfoDto.getNickname().equals(user.getNickname())) {
                    log.warn("Nickname '{}' already exists", additionalInfoDto.getNickname());
                    throw new RestApiException(ALREADY_EXIST_NICKNAME);
                }
                user.setNickname(additionalInfoDto.getNickname());
            }

            user.setPhone(additionalInfoDto.getPhoneNumber() != null ? additionalInfoDto.getPhoneNumber() : user.getPhone());
            user.setGender(additionalInfoDto.getGender() != null ? Gender.valueOf(additionalInfoDto.getGender()) : user.getGender());
            user.setBirthDate(additionalInfoDto.getBirthDate() != null ? additionalInfoDto.getBirthDate() : user.getBirthDate());

            if (additionalInfoDto.getInterests() != null) {
                List<Category> interests = additionalInfoDto.getInterests().stream()
                        .map(interest -> categoryRepository.findByName(CategoryType.valueOf(interest)))
                        .collect(Collectors.toList());
                user.setInterests(interests);
            }
        }

        if (profileImage != null) {
            String profileImageUrl = s3Service.uploadUserProfileImage(profileImage);
            user.setProfileImageUrl(profileImageUrl);
        }

        userRepository.save(user);
    }

    public String getCurrentUserEmail() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RestApiException(NOT_AUTHENTICATED_USER);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUsername();
        } else {
            throw new RestApiException(UNEXPECTED_PRINCIPAL_TYPE);
        }
    }
}
