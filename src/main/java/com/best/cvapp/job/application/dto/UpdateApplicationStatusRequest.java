package com.best.cvapp.job.application.dto;

import com.best.cvapp.job.core.ApplicationStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.EnumSet;
import java.util.Set;

public record UpdateApplicationStatusRequest(
        @NotNull(message = "Application status is required")
        ApplicationStatus status
) {

    private static final Set<ApplicationStatus> COMPANY_STATUSES = EnumSet.of(
            ApplicationStatus.REVIEWED,
            ApplicationStatus.SHORTLISTED,
            ApplicationStatus.CONTACTED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.ACCEPTED
    );

    @AssertTrue(message = "Application status cannot be set by a company")
    public boolean isCompanyStatusAllowed() {
        return status == null || COMPANY_STATUSES.contains(status);
    }

    public static boolean isAllowedCompanyStatus(ApplicationStatus status) {
        return status != null && COMPANY_STATUSES.contains(status);
    }
}
