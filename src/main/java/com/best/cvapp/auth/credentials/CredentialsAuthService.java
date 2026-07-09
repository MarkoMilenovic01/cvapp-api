package com.best.cvapp.auth.credentials;

import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.auth.session.AuthSessionService;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.shared.exceptions.EmailAlreadyInUseException;
import com.best.cvapp.shared.exceptions.GoogleAccountLoginRequiredException;
import com.best.cvapp.shared.exceptions.PasswordsDoNotMatchException;
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

@Service
@RequiredArgsConstructor
public class CredentialsAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthSessionService authSessionService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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
                .enabled(true)
                .build();

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyInUseException();
        }

        return authSessionService.createSession(user);
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