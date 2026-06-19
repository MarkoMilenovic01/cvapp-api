package com.best.cvapp.auth.token.invitecompany;

import com.best.cvapp.auth.token.jwt.JwtService;
import com.best.cvapp.auth.dto.AuthResponse;
import com.best.cvapp.auth.token.refresh.RefreshToken;
import com.best.cvapp.auth.token.refresh.RefreshTokenService;
import com.best.cvapp.email.EmailService;
import com.best.cvapp.user.AuthProvider;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyInviteService {

    private final CompanyInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.invite.expiration-hours}")
    private int expirationHours;

    public void sendInvite(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already has an account");
        }
        if (inviteRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invite already sent to this email");
        }

        String token = UUID.randomUUID().toString();

        CompanyInvite invite = CompanyInvite.builder()
                .email(email)
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .build();

        inviteRepository.save(invite);
        emailService.sendCompanyInvite(email, token);
    }

    public AuthResponse acceptInvite(String token, String password) {
        CompanyInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invite token"));

        if (invite.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Invite already used");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Invite has expired");
        }

        User user = User.builder()
                .email(invite.getEmail())
                .password(passwordEncoder.encode(password))
                .role(Role.COMPANY)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        userRepository.save(user);

        invite.setUsed(true);
        inviteRepository.save(invite);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getRole().name());
    }
}