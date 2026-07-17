package com.best.cvapp.cv.profile.dto;

import jakarta.validation.constraints.Size;

public record CVRequest(

        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone,

        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,

        @Size(max = 5000, message = "Summary must be at most 5000 characters")
        String summary,

        @Size(max = 255, message = "LinkedIn URL must be at most 255 characters")
        String linkedinUrl,

        @Size(max = 255, message = "GitHub URL must be at most 255 characters")
        String githubUrl
) {
}
