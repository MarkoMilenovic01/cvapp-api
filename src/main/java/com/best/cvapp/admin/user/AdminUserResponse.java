package com.best.cvapp.admin.user;

import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.user.Role;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled,
        AuthProvider provider,
        LocalDateTime createdAt
) {}