package com.best.cvapp.auth.oauth;

import com.best.cvapp.user.AuthProvider;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo info = OAuth2UserInfo.from(oAuth2User);

        userRepository.findByEmail(info.email()).ifPresentOrElse(
                existingUser -> {
                    if (existingUser.getProvider() != AuthProvider.GOOGLE) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "This email is already registered with a password. Please login normally.");
                    }
                },
                () -> userRepository.save(User.builder()
                        .email(info.email())
                        .password(UUID.randomUUID().toString())
                        .role(Role.USER)
                        .provider(AuthProvider.GOOGLE)
                        .enabled(true)
                        .build())
        );

        return oAuth2User;
    }
}