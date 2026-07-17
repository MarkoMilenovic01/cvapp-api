package com.best.cvapp.cv.project.dto;

import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        String projectUrl,
        String repositoryUrl,
        LocalDate startDate,
        LocalDate endDate,
        boolean current
) {
}