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

        if ("google".equals(registrationId)) {
            // Google API 호출
            UserDto userDto = userService.getUserFromGoogle(accessToken);

            Optional<User> existingUserOptional = userService.findByEmail(userDto.getEmail());
            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                if (existingUser.getAuthType() != AuthType.GOOGLE) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("already_registered_other_auth_type"),
                            "User already registered with a different auth type");
                }
            }

            return new CustomOAuth2User(userDto);
        } else {
            throw new OAuth2AuthenticationException(new OAuth2Error("not_supported_auth_type"),
                    "Unsupported OAuth2 provider");
        }
    }
}