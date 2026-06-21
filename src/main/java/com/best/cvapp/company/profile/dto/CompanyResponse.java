package com.best.cvapp.company.profile.dto;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long id,
        String name,
        String description,
        String website,
        String industry,
        String photoUrl,
        LocalDateTime createdAt
) {
}