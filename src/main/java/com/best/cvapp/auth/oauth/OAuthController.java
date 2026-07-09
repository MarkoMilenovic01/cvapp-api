package com.best.cvapp.auth.oauth;

import com.best.cvapp.auth.oauth.dto.OAuthExchangeRequest;
import com.best.cvapp.auth.session.dto.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @PostMapping("/exchange")
    public ResponseEntity<AuthResponse> exchange(@Valid @RequestBody OAuthExchangeRequest request) {
        AuthResponse response = oAuthService.exchangeCode(request);
        return ResponseEntity.ok(response);
    }
}