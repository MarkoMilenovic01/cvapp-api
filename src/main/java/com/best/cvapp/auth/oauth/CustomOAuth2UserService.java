package com.best.cvapp.auth.oauth;

import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo info = OAuth2UserInfo.from(oAuth2User);

        if (info.email() == null || info.email().isBlank()) {
            throw error("missing_email", "Google account email is missing");
        }

        userRepository.findByEmail(info.email()).ifPresentOrElse(
                existingUser -> {
                    if (existingUser.getRole() != Role.USER) {
                        throw error("role_not_allowed", "Google login is only allowed for user accounts");
                    }
                    if (existingUser.getProvider() != AuthProvider.GOOGLE) {
                        throw error("provider_conflict", "This email is already registered with a password");
                    }
                    if (!existingUser.isEnabled()) {
                        throw error("account_disabled", "Account is disabled");
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

    private OAuth2AuthenticationException error(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code, description, null));
    }
}