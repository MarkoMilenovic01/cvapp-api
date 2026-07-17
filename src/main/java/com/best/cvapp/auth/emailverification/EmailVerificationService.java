package com.best.cvapp.auth.emailverification;

import com.best.cvapp.email.EmailService;
import com.best.cvapp.auth.emailverification.exception.InvalidVerificationTokenException;
import com.best.cvapp.auth.emailverification.exception.VerificationTokenExpiredException;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles email verification.
 *
 * Flow:
 * 1. Replace any existing verification token.
 * 2. Create and email a new token.
 * 3. Validate the submitted token.
 * 4. Enable the user and delete the token.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final long EXPIRATION_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void createAndSendVerification(User user) {
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        String rawToken = UUID.randomUUID().toString();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(DigestUtils.sha256Hex(rawToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .build();

        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public void verify(String rawToken) {
        EmailVerificationToken token = tokenRepository.findByToken(DigestUtils.sha256Hex(rawToken))
                .orElseThrow(InvalidVerificationTokenException::new);

        if (token.isExpired()) {
            tokenRepository.delete(token);
            throw new VerificationTokenExpiredException();
        }

        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(token);
    }
}
