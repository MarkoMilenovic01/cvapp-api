package com.best.cvapp.job.core;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        Long companyId,
        String companyName,
        String title,
        String description,
        String requirements,
        String location,
        EmploymentType employmentType,
        WorkMode workMode,
        LocalDate deadline,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}