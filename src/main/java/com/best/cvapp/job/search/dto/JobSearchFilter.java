package com.best.cvapp.job.search.dto;

import com.best.cvapp.job.core.EmploymentType;
import com.best.cvapp.job.core.WorkMode;
import jakarta.validation.constraints.Size;

public record JobSearchFilter(
        @Size(max = 200, message = "Keyword must be at most 200 characters")
        String keyword,

        @Size(max = 255, message = "Location must be at most 255 characters")
        String location,

        WorkMode workMode,

        EmploymentType employmentType,

        @Size(max = 255, message = "Company name must be at most 255 characters")
        String companyName
) {
}
