package com.best.cvapp.company.cv.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record CompanyCVSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String summary,
        boolean favorite
) {
}