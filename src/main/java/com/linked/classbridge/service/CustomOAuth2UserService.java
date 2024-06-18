package com.linked.classbridge.service;

import com.linked.classbridge.dto.user.CustomOAuth2User;
import com.linked.classbridge.dto.user.UserDto;
import com.linked.classbridge.domain.User;
import com.linked.classbridge.type.AuthType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("Loading user from OAuth2 provider: {}", userRequest.getClientRegistration().getRegistrationId());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();
        log.debug("Received access token: {}", accessToken);

        if ("google".equals(registrationId)) {
            log.info("Handling Google OAuth2 login");

            // Google API 호출
            UserDto userDto = userService.getUserFromGoogle(accessToken);
            log.info("Retrieved user from Google: {}", userDto.getUsername());

            Optional<User> existingUserOptional = userService.findByEmail(userDto.getEmail());
            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                log.info("User with email '{}' already exists", userDto.getEmail());

                if (existingUser.getAuthType() != AuthType.GOOGLE) {
                    log.warn("User with email '{}' registered with a different auth type", userDto.getEmail());
                    throw new OAuth2AuthenticationException(new OAuth2Error("already_registered_other_auth_type"),
                            "User already registered with a different auth type");
                }
            } else {
                log.info("User with email '{}' is new, proceeding with registration", userDto.getEmail());
            }

            return new CustomOAuth2User(userDto);
        } else {
            log.warn("Unsupported OAuth2 provider: {}", registrationId);
            throw new OAuth2AuthenticationException(new OAuth2Error("not_supported_auth_type"),
                    "Unsupported OAuth2 provider");
        }
    }
}