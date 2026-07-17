package com.best.cvapp.job.company.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateJobActiveRequest(
        @NotNull(message = "Active state is required")
        Boolean active
) {
}
