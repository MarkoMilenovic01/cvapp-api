package com.best.cvapp.admin.job;

import com.best.cvapp.job.core.EmploymentType;
import com.best.cvapp.job.core.WorkMode;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminJobResponse(
        Long id,
        Long companyId,
        String companyName,
        String title,
        String location,
        EmploymentType employmentType,
        WorkMode workMode,
        LocalDate deadline,
        boolean active,
        LocalDateTime createdAt
) {}