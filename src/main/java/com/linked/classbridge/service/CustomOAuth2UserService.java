package com.linked.classbridge.service;

import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.GoogleResponse;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.type.AuthType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        System.out.println("OAuth2 login attempt for registrationId: " + registrationId);

        if ("google".equals(registrationId)) {
            // Google API 호출
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

            Map attributes = response.getBody();
            GoogleResponse googleResponse = new GoogleResponse(attributes);

            Optional<User> existingUserOptional = userService.findByEmail(googleResponse.getEmail());
            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                if (existingUser.getAuthType() != AuthType.GOOGLE) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("already_registered_other_auth_type"),
                            "User already registered with a different auth type");
                }

                UserDto userDTO = convertToDTO(existingUser);

                return new CustomOAuth2User(userDTO);
            } else {
                throw new OAuth2AuthenticationException(new OAuth2Error("user_not_registered"), "User not registered, proceed with registration");
            }
        } else {
            throw new OAuth2AuthenticationException(new OAuth2Error("not_supported_auth_type"), "Unsupported OAuth2 provider");
        }
    }

    private UserDto convertToDTO(User user) {
        UserDto userDTO = new UserDto();
        userDTO.setProvider(user.getProvider());
        userDTO.setProviderId(user.getProviderId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setAuthType(user.getAuthType());

        Hibernate.initialize(user.getRoles());

        List<String> roles = user.getRoles().stream()
                .map(role -> role.name().substring(5)) // "ROLE_" prefix 제거
                .collect(Collectors.toList());
        userDTO.setRoles(roles);
        return userDTO;
    }
}