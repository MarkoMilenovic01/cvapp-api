package com.best.cvapp.auth.passwordreset;

import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.auth.passwordreset.dto.ForgotPasswordRequest;
import com.best.cvapp.auth.passwordreset.dto.PasswordResetRequest;
import com.best.cvapp.auth.session.RefreshTokenService;
import com.best.cvapp.email.EmailService;
import com.best.cvapp.auth.credentials.exception.GoogleAccountLoginRequiredException;
import com.best.cvapp.auth.passwordreset.exception.InvalidResetTokenException;
import com.best.cvapp.auth.passwordreset.exception.NoAccountFoundException;
import com.best.cvapp.auth.credentials.exception.PasswordsDoNotMatchException;
import com.best.cvapp.auth.passwordreset.exception.ResetTokenAlreadyUsedException;
import com.best.cvapp.auth.passwordreset.exception.ResetTokenExpiredException;
import com.best.cvapp.auth.passwordreset.exception.UserNotFoundException;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles password resets.
 *
 * Flow:
 * 1. Find the local account by email.
 * 2. Create and email a reset token.
 * 3. Validate the token and new passwords.
 * 4. Update the password and remove existing sessions.
 * 5. Mark the reset token as used.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.password-reset.expiration-hours:1}")
    private int passwordResetExpirationHours;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(NoAccountFoundException::new);

        if (user.getProvider() == AuthProvider.GOOGLE) {
            throw new GoogleAccountLoginRequiredException();
        }

        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(DigestUtils.sha256Hex(token))
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(passwordResetExpirationHours))
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(email, token);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordsDoNotMatchException();
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(
                        DigestUtils.sha256Hex(request.token()))
                .orElseThrow(InvalidResetTokenException::new);

        if (resetToken.isUsed()) {
            throw new ResetTokenAlreadyUsedException();
        }

        if (resetToken.isExpired()) {
            throw new ResetTokenExpiredException();
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(UserNotFoundException::new);

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        refreshTokenService.deleteByUser(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
