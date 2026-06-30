package com.best.cvapp.admin.company.dto;

import java.time.LocalDateTime;

public record AdminCompanyResponse(
        Long id,
        Long userId,
        String email,
        String name,
        String description,
        String website,
        String industry,
        String photoUrl,
        LocalDateTime createdAt
) {}