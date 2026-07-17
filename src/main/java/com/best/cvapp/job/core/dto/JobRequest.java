package com.best.cvapp.job.core.dto;

import com.best.cvapp.job.core.EmploymentType;
import com.best.cvapp.job.core.WorkMode;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record JobRequest(
        @NotBlank(message = "Job title is required")
        @Size(max = 255, message = "Job title must be at most 255 characters")
        String title,

        @NotBlank(message = "Job description is required")
        @Size(max = 10000, message = "Job description must be at most 10000 characters")
        String description,

        @Size(max = 10000, message = "Job requirements must be at most 10000 characters")
        String requirements,

        @Size(max = 255, message = "Job location must be at most 255 characters")
        String location,

        @NotNull(message = "Employment type is required")
        EmploymentType employmentType,

        @NotNull(message = "Work mode is required")
        WorkMode workMode,

        @FutureOrPresent(message = "Job deadline cannot be in the past")
        LocalDate deadline
) {
}
