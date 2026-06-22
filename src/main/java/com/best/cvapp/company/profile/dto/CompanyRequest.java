package com.best.cvapp.company.profile.dto;

public record CompanyRequest(
        String name,
        String description,
        String website,
        String industry
) {
}