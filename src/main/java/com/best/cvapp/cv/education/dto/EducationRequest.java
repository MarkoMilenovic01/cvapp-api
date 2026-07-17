package com.best.cvapp.cv.education.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EducationRequest(

        @NotBlank(message = "Institution is required")
        @Size(max = 255, message = "Institution must be at most 255 characters")
        String institution,

        @Size(max = 255, message = "Degree must be at most 255 characters")
        String degree,

        @Size(max = 255, message = "Field of study must be at most 255 characters")
        String fieldOfStudy,

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

        @AssertTrue(message = "Current education cannot have an end date")
        public boolean isCurrentEducationValid() {
                return !current || endDate == null;
        }
}