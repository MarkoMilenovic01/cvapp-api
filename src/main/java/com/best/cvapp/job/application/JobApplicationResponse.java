package com.best.cvapp.job.application;

import com.best.cvapp.job.core.ApplicationStatus;

import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long companyId,
        String companyName,
        Long userId,
        Long cvId,
        String cvFirstName,
        String cvLastName,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt
) {}