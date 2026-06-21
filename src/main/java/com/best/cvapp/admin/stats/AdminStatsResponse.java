package com.best.cvapp.admin.stats;

public record AdminStatsResponse(
        long totalUsers,
        long totalCompanies,
        long totalCVs,
        long totalJobs,
        long activeJobs,
        long inactiveJobs,
        long totalApplications
) {}