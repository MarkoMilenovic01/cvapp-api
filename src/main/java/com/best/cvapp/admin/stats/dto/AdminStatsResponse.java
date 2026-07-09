package com.best.cvapp.admin.stats.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalCompanies,
        long totalCVs,
        long totalJobs,
        long activeJobs,
        long inactiveJobs,
        long totalApplications
) {}