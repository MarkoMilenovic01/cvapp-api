package com.best.cvapp.auth.emailverification;

import com.best.cvapp.email.EmailService;
import com.best.cvapp.auth.emailverification.exception.InvalidVerificationTokenException;
import com.best.cvapp.auth.emailverification.exception.VerificationTokenExpiredException;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Creates, sends, and verifies email verification tokens issued at
 * registration. A user stays disabled until their token is verified.
 *
 * Flow:
 * 1. Create and send - replace any existing token for the user, issue
 *                        a new one, and email it.
 * 2. Verify           - look up the token, reject (and delete) it if
 *                        expired, otherwise enable the user and delete
 *                        the token.
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

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .build();

        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), token.getToken());
    }

    @Transactional
    public void verify(String rawToken) {
        EmailVerificationToken token = tokenRepository.findByToken(rawToken)
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