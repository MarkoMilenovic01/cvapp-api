package com.best.cvapp.company.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must be at most 255 characters")
        String name,

        @Size(max = 5000, message = "Description must be at most 5000 characters")
        String description,

        @Size(max = 255, message = "Website must be at most 255 characters")
        @Pattern(
                regexp = "^(?:https?://\\S+)?$",
                message = "Website must be a valid HTTP or HTTPS URL"
        )
        String website,

        @Size(max = 100, message = "Industry must be at most 100 characters")
        String industry
) {
}
