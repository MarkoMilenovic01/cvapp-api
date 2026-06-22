package com.best.cvapp.auth.passwordreset;

import com.best.cvapp.auth.passwordreset.dto.ForgotPasswordRequest;
import com.best.cvapp.auth.passwordreset.dto.PasswordResetRequest;
import com.best.cvapp.email.EmailService;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.password-reset.expiration-hours:1}")
    private int passwordResetExpirationHours;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No account found with this email"
                ));

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account uses Google login. Please sign in with Google."
            );
        }

        passwordResetTokenRepository.deleteByEmail(request.email());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(request.email())
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(passwordResetExpirationHours))
                .build();

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(request.email(), token);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invalid reset token"
                ));

        if (resetToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Reset token already used");
        }

        if (resetToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Reset token has expired");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}