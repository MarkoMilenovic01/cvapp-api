package com.best.cvapp.company.cv.dto;

public record CompanyCVSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String summary,
        boolean favorite
) {
}
