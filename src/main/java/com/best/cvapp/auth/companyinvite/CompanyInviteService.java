package com.best.cvapp.auth.companyinvite;

import com.best.cvapp.auth.companyinvite.dto.AcceptInviteRequest;
import com.best.cvapp.auth.companyinvite.dto.InviteRequest;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.AuthSessionService;
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
import org.springframework.transaction.annotation.Transactional;
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
    private final AuthSessionService authSessionService;

    @Value("${app.invite.expiration-hours}")
    private int expirationHours;

    @Transactional
    public void sendInvite(InviteRequest request) {
        String email = request.email().trim().toLowerCase();

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

    @Transactional
    public AuthResponse acceptInvite(AcceptInviteRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        CompanyInvite invite = inviteRepository.findByToken(request.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invite token"));

        if (invite.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Invite already used");
        }

        if (invite.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Invite has expired");
        }

        if (userRepository.existsByEmail(invite.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already has an account");
        }

        User user = User.builder()
                .email(invite.getEmail())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.COMPANY)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        userRepository.save(user);

        invite.setUsed(true);
        inviteRepository.save(invite);

        return authSessionService.createSession(user);
    }
}