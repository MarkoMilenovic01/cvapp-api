package com.best.cvapp.company.cv.dto;

import jakarta.validation.constraints.Size;

public record CVSearchRequest(
        @Size(max = 200, message = "Keyword must be at most 200 characters")
        String keyword,

        @Size(max = 100, message = "Skill must be at most 100 characters")
        String skill,

        @Size(max = 255, message = "Location must be at most 255 characters")
        String location
) {
}
