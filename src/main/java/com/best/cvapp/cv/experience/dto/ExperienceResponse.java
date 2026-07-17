package com.best.cvapp.cv.experience.dto;

import com.best.cvapp.cv.experience.ExperienceType;

import java.time.LocalDate;

public record ExperienceResponse(
        Long id,
        String companyName,
        String position,
        ExperienceType experienceType,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean current
) {
}