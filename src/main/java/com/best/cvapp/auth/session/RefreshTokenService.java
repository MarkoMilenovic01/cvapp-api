package com.best.cvapp.auth.session;

import com.best.cvapp.auth.session.exception.InvalidRefreshTokenException;
import com.best.cvapp.auth.session.exception.RefreshTokenExpiredException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles refresh tokens.
 *
 * Flow:
 * 1. Generate a random token.
 * 2. Store its hash and return the raw token.
 * 3. Validate the hash and expiration when the token is used.
 * 4. Delete the token when it expires or the session ends.
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
                .token(DigestUtils.sha256Hex(rawToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(
                        DigestUtils.sha256Hex(rawToken))
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

}
