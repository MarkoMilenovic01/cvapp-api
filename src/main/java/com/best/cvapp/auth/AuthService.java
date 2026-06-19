package com.best.cvapp.auth;

import com.best.cvapp.auth.token.invitecompany.CompanyInviteService;
import com.best.cvapp.auth.token.invitecompany.AcceptInviteRequest;
import com.best.cvapp.auth.dto.AuthResponse;
import com.best.cvapp.auth.token.jwt.JwtService;
import com.best.cvapp.auth.dto.LoginRequest;
import com.best.cvapp.auth.dto.RegisterRequest;
import com.best.cvapp.auth.token.passwordreset.ForgotPasswordRequest;
import com.best.cvapp.auth.token.passwordreset.PasswordResetRequest;
import com.best.cvapp.auth.token.passwordreset.PasswordResetToken;
import com.best.cvapp.auth.token.passwordreset.PasswordResetTokenRepository;
import com.best.cvapp.auth.token.refresh.RefreshToken;
import com.best.cvapp.auth.token.refresh.RefreshTokenRequest;
import com.best.cvapp.auth.token.refresh.RefreshTokenService;
import com.best.cvapp.email.EmailService;
import com.best.cvapp.user.AuthProvider;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final CompanyInviteService companyInviteService;
    private final EmailService emailService;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.password-reset.expiration-hours:1}")
    private int passwordResetExpirationHours;

    public AuthResponse register(RegisterRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            if (existingUser.getProvider() == AuthProvider.GOOGLE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This email is registered with Google. Please login with Google.");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        });

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getRole().name());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new AuthResponse(newAccessToken, refreshToken.getToken(), user.getRole().name());
    }

    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        refreshTokenService.deleteByUser(refreshToken.getUser());
    }

    public AuthResponse acceptInvite(AcceptInviteRequest request) {
        return companyInviteService.acceptInvite(request.getToken(), request.getPassword());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found with this email"));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This account uses Google login. Please sign in with Google.");
        }

        // Delete any existing reset tokens for this email
        passwordResetTokenRepository.deleteByEmail(request.getEmail());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(request.getEmail())
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(passwordResetExpirationHours))
                .build();

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(request.getEmail(), token);
    }

    public void resetPassword(PasswordResetRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Reset token already used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Reset token has expired");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}