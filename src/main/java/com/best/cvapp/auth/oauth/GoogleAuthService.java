package com.best.cvapp.auth.oauth;

import com.best.cvapp.auth.oauth.dto.GoogleLoginRequest;
import com.best.cvapp.auth.session.AuthSessionService;
import com.best.cvapp.auth.session.dto.AuthResponse;
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
import java.util.UUID;

/**
 * Handles login with Google.
 *
 * Flow:
 * 1. Verify   - check the ID token with Google directly (audience must
 *                match our client id, email must be verified).
 * 2. Match    - look up the verified email; if it belongs to an account
 *                created a different way (e.g. local email/password),
 *                reject instead of silently logging into that account.
 * 3. Register - if no account exists yet, create one automatically with
 *                a random, unusable password, since the user will always
 *                log in through Google, never a password.
 * 4. Session  - create a normal session, same as local login.
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
    public AuthResponse login(GoogleLoginRequest request) {
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
            if (Boolean.FALSE.equals(payload.getEmailVerified())) {
                throw new InvalidOAuthTokenException();
            }

            return payload.getEmail();
        } catch (InvalidOAuthTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidOAuthTokenException();
        }
    }
}