package com.best.cvapp.admin.stats;

import com.best.cvapp.admin.stats.dto.AdminStatsResponse;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.job.application.JobApplicationRepository;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final UserRepository           userRepository;
    private final CompanyRepository        companyRepository;
    private final CVRepository             cvRepository;
    private final JobRepository            jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public AdminStatsResponse getStats() {
        long totalJobs    = jobRepository.count();
        long activeJobs   = jobRepository.countByActiveTrue();
        return new AdminStatsResponse(
                userRepository.count(),
                companyRepository.count(),
                cvRepository.count(),
                totalJobs,
                activeJobs,
                totalJobs - activeJobs,
                jobApplicationRepository.count()
        );
    }
}