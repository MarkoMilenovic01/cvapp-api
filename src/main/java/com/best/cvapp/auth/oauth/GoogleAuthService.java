package com.best.cvapp.auth.oauth;

import com.best.cvapp.auth.oauth.dto.GoogleLoginRequest;
import com.best.cvapp.auth.session.AuthSessionService;
import com.best.cvapp.auth.session.dto.SessionTokens;
import com.best.cvapp.auth.oauth.exception.InvalidOAuthTokenException;
import com.best.cvapp.auth.oauth.exception.OAuthAccountConflictException;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles Google login.
 *
 * Flow:
 * 1. Verify the Google ID token and email.
 * 2. Find the user by email.
 * 3. Create a Google user if one does not exist.
 * 4. Create a session.
 */
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final AuthSessionService authSessionService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.oauth.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    void init() {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    @Transactional
    public SessionTokens login(GoogleLoginRequest request) {
        String email = verify(request.idToken());

        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    if (existing.getProvider() != AuthProvider.GOOGLE) {
                        throw new OAuthAccountConflictException();
                    }
                    return existing;
                })
                .orElseGet(() -> registerNewGoogleUser(email));

        return authSessionService.createSession(user);
    }

    private User registerNewGoogleUser(String email) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER)
                .provider(AuthProvider.GOOGLE)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    private String verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidOAuthTokenException();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new InvalidOAuthTokenException();
            }

            String email = payload.getEmail();
            if (email == null || email.isBlank()) {
                throw new InvalidOAuthTokenException();
            }

            return email.trim().toLowerCase(Locale.ROOT);
        } catch (InvalidOAuthTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidOAuthTokenException();
        }
    }
}
