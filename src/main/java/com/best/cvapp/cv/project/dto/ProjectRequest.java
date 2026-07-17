package com.best.cvapp.cv.project.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must be at most 255 characters")
        String name,

        @Size(max = 5000, message = "Description must be at most 5000 characters")
        String description,

        @Size(max = 500, message = "Project URL must be at most 500 characters")
        String projectUrl,

        @Size(max = 500, message = "Repository URL must be at most 500 characters")
        String repositoryUrl,

        LocalDate startDate,

        LocalDate endDate,

        boolean current
) {

    @AssertTrue(message = "End date cannot be before start date")
    public boolean isDateRangeValid() {
        return startDate == null
                || endDate == null
                || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Current project cannot have an end date")
    public boolean isCurrentProjectValid() {
        return !current || endDate == null;
    }
}
