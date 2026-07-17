package com.best.cvapp.cv.experience.dto;

import com.best.cvapp.cv.experience.ExperienceType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExperienceRequest(

        @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must be at most 255 characters")
        String companyName,

        @NotBlank(message = "Position is required")
        @Size(max = 255, message = "Position must be at most 255 characters")
        String position,

        @NotNull(message = "Experience type is required")
        ExperienceType experienceType,

        @Size(max = 5000, message = "Description must be at most 5000 characters")
        String description,

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

        @AssertTrue(message = "Current experience cannot have an end date")
        public boolean isCurrentExperienceValid() {
                return !current || endDate == null;
        }
}
