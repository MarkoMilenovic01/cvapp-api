package com.best.cvapp.auth.session;

import com.best.cvapp.auth.session.exception.InvalidRefreshTokenException;
import com.best.cvapp.auth.session.exception.RefreshTokenExpiredException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Manages refresh tokens: creating them, validating them, and deleting them.
 *
 * Only the SHA-256 hash of a token is ever persisted, so a database leak
 * alone doesn't hand out usable tokens - the raw value is returned to the
 * caller once, at creation, and never stored. If a token doesn't exist or
 * has expired, validation throws instead of returning a user, and expired
 * tokens are deleted as soon as they're found.
 *
 * Flow:
 * 1. Create   - generate a random token, hash it, persist the hash,
 *                return the raw token to the caller.
 * 2. Validate - hash the incoming token, look it up, reject if missing
 *                or expired (deleting expired ones on the way out).
 * 3. Delete   - by user (all sessions) or by id (a single session).
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(hashToken(rawToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashToken(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException();
        }

        return refreshToken;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteById(Long id) {
        refreshTokenRepository.findById(id).ifPresent(refreshTokenRepository::delete);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}