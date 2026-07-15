package com.best.cvapp.auth.credentials;

import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.credentials.dto.RegisterResponse;
import com.best.cvapp.auth.emailverification.EmailVerificationService;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.auth.session.AuthSessionService;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.credentials.exception.EmailAlreadyInUseException;
import com.best.cvapp.auth.credentials.exception.GoogleAccountLoginRequiredException;
import com.best.cvapp.auth.credentials.exception.PasswordsDoNotMatchException;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Handles login and registration with email and password.
 *
 * Flow:
 * 1. Register - check passwords    match, normalize email, reject if the email
 *                is already taken (pointing Google accounts to Google login
 *                instead), hash the password, save the user as disabled,
 *                and send a verification email. Returns a generic message,
 *                never confirming or denying account state to the caller.
 * 2. Login    - normalize email, authenticate through Spring Security
 *                (handles bad credentials and disabled accounts), then
 *                create a new session for the authenticated user.
 */
@Service
@RequiredArgsConstructor
public class CredentialsAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthSessionService authSessionService;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordsDoNotMatchException();
        }

        String email = normalizeEmail(request.email());

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (existingUser.getProvider() == AuthProvider.GOOGLE) {
                throw new GoogleAccountLoginRequiredException();
            }
            throw new EmailAlreadyInUseException();
        });

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .enabled(false)
                .build();

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyInUseException();
        }

        emailVerificationService.createAndSendVerification(user);

        return new RegisterResponse("Registration successful. Please check your email to verify your account.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = (User) auth.getPrincipal();

        return authSessionService.createSession(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}