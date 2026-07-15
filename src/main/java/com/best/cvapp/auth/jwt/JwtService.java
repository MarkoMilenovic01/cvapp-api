package com.best.cvapp.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;


/**
 * Service responsible for creating and validating JWT access tokens.
 *
 * Flow when generating a token:
 * 1. Takes the authenticated user's details.
 * 2. Stores the user's email as the JWT subject.
 * 3. Stores the user's role as a custom claim.
 * 4. Adds issue time and expiration time.
 * 5. Signs the token using the configured secret key.
 *
 * Flow when validating a token:
 * 1. Verifies the token signature using the configured secret key.
 * 2. Extracts the email from the token subject.
 * 3. Checks that the email matches the loaded user.
 * 4. Checks that the user account is still enabled.
 * 5. Checks that the token has not expired.
 *
 * Access tokens are stateless. They are not stored in the database.
 * Once issued, they remain valid until expiration unless the user is
 * disabled, because validation also checks the user's enabled status.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername())
                && userDetails.isEnabled()
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}