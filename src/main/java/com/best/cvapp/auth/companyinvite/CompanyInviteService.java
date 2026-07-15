package com.best.cvapp.auth.companyinvite;

import com.best.cvapp.auth.companyinvite.dto.AcceptInviteRequest;
import com.best.cvapp.auth.companyinvite.dto.InviteRequest;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.auth.session.AuthSessionService;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.email.EmailService;
import com.best.cvapp.auth.credentials.exception.EmailAlreadyInUseException;
import com.best.cvapp.auth.companyinvite.exception.InvalidOrExpiredInviteTokenException;
import com.best.cvapp.auth.companyinvite.exception.InviteAlreadySentException;
import com.best.cvapp.auth.credentials.exception.PasswordsDoNotMatchException;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles inviting a company to join and turning that invite into an
 * account.
 *
 * Flow:
 * 1. Send invite   - admin-only. Reject if the email already has an
 *                      account, or already has a pending (non-expired,
 *                      unused) invite. Replace any stale invite, then
 *                      email a new token.
 * 2. Accept invite - check passwords match, validate the token (must
 *                      exist, be unused, and not be expired), re-check
 *                      the email is still free, create a COMPANY user
 *                      and its Company profile, mark the invite used,
 *                      and start a session.
 */
@Service
@RequiredArgsConstructor
public class CompanyInviteService {

    private final CompanyInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthSessionService authSessionService;

    @Value("${app.invite.expiration-hours}")
    private int expirationHours;

    @Transactional
    public void sendInvite(InviteRequest request) {
        String email = normalizeEmail(request.email());
        String companyName = normalizeCompanyName(request.companyName());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException();
        }

        inviteRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.isExpired() && !existing.isUsed()) {
                throw new InviteAlreadySentException();
            }
            inviteRepository.delete(existing);
            inviteRepository.flush();
        });

        String token = UUID.randomUUID().toString();

        CompanyInvite invite = CompanyInvite.builder()
                .email(email)
                .companyName(companyName)
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
            throw new PasswordsDoNotMatchException();
        }

        CompanyInvite invite = inviteRepository.findByToken(request.token())
                .orElseThrow(InvalidOrExpiredInviteTokenException::new);

        if (invite.isUsed() || invite.isExpired()) {
            throw new InvalidOrExpiredInviteTokenException();
        }

        if (userRepository.existsByEmail(invite.getEmail())) {
            throw new EmailAlreadyInUseException();
        }

        User user = User.builder()
                .email(invite.getEmail())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.COMPANY)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyInUseException();
        }

        Company company = Company.builder()
                .user(user)
                .name(invite.getCompanyName())
                .build();

        companyRepository.save(company);

        invite.setUsed(true);
        inviteRepository.save(invite);

        return authSessionService.createSession(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCompanyName(String companyName) {
        return companyName.trim().replaceAll("\\s+", " ");
    }
}