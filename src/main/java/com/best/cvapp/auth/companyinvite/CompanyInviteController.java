package com.best.cvapp.auth.companyinvite;

import com.best.cvapp.auth.companyinvite.dto.AcceptInviteRequest;
import com.best.cvapp.auth.companyinvite.dto.InviteRequest;
import com.best.cvapp.auth.session.dto.AuthResponse;
import com.best.cvapp.auth.session.RefreshTokenCookieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/company-invites")
@RequiredArgsConstructor
public class CompanyInviteController {

    private final CompanyInviteService companyInviteService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendInvite(@Valid @RequestBody InviteRequest request) {
        companyInviteService.sendInvite(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<AuthResponse> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        return refreshTokenCookieService.authenticated(companyInviteService.acceptInvite(request));
    }
}
