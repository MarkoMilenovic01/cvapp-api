package com.best.cvapp.job.company;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyProfileService;
import com.best.cvapp.job.application.JobApplication;
import com.best.cvapp.job.application.JobApplicationMapper;
import com.best.cvapp.job.application.JobApplicationRepository;
import com.best.cvapp.job.application.dto.JobApplicationResponse;
import com.best.cvapp.job.application.dto.UpdateApplicationStatusRequest;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobMapper;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.job.core.dto.JobRequest;
import com.best.cvapp.job.core.dto.JobResponse;
import com.best.cvapp.job.company.exception.CompanyJobApplicationNotFoundException;
import com.best.cvapp.job.company.exception.CompanyJobNotFoundException;
import com.best.cvapp.job.company.exception.CannotActivateExpiredJobException;
import com.best.cvapp.job.company.exception.InvalidCompanyApplicationStatusException;
import com.best.cvapp.job.company.dto.UpdateJobActiveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

/**
 * Handles job management and application review for authenticated companies.
 *
 * Flow:
 * 1. Load the company belonging to the authenticated user.
 * 2. Create, list, update, activate, deactivate, or delete company-owned jobs.
 * 3. Prevent expired jobs from being reactivated.
 * 4. Load applications only for jobs belonging to the company.
 * 5. Validate and save company-managed application statuses.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyJobService {

    private final JobRepository            jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyProfileService    companyProfileService;
    private final JobMapper                jobMapper;
    private final JobApplicationMapper     jobApplicationMapper;

    // ── Job Management ────────────────────────────────────────────────────────

    @Transactional
    public JobResponse createJob(JobRequest request) {
        Company company = companyProfileService.getAuthenticatedCompany();

        Job job = Job.builder()
                .company(company)
                .title(request.title())
                .description(request.description())
                .requirements(request.requirements())
                .location(request.location())
                .employmentType(request.employmentType())
                .workMode(request.workMode())
                .deadline(request.deadline())
                .active(true)
                .build();

        return jobMapper.toResponse(jobRepository.save(job));
    }

    public Page<JobResponse> getMyJobs(Pageable pageable) {
        Company company = companyProfileService.getAuthenticatedCompany();
        return jobRepository.findByCompany(company, pageable)
                .map(jobMapper::toResponse);
    }

    public JobResponse getMyJobById(Long id) {
        Company company = companyProfileService.getAuthenticatedCompany();
        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(CompanyJobNotFoundException::new);
        return jobMapper.toResponse(job);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request) {
        Company company = companyProfileService.getAuthenticatedCompany();

        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(CompanyJobNotFoundException::new);

        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setRequirements(request.requirements());
        job.setLocation(request.location());
        job.setEmploymentType(request.employmentType());
        job.setWorkMode(request.workMode());
        job.setDeadline(request.deadline());

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse updateJobActiveState(Long id, UpdateJobActiveRequest request) {
        Company company = companyProfileService.getAuthenticatedCompany();
        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(CompanyJobNotFoundException::new);

        if (Boolean.TRUE.equals(request.active())
                && job.getDeadline() != null
                && job.getDeadline().isBefore(LocalDate.now())) {
            throw new CannotActivateExpiredJobException();
        }

        job.setActive(request.active());
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        Company company = companyProfileService.getAuthenticatedCompany();
        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(CompanyJobNotFoundException::new);
        jobRepository.delete(job);
    }

    // ── Application Review ────────────────────────────────────────────────────

    public List<JobApplicationResponse> getApplicationsForJob(Long jobId) {
        Company company = companyProfileService.getAuthenticatedCompany();
        Job job = jobRepository.findByIdAndCompany(jobId, company)
                .orElseThrow(CompanyJobNotFoundException::new);
        return jobApplicationRepository.findByJobOrderByAppliedAtDesc(job).stream()
                .map(jobApplicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public JobApplicationResponse updateApplicationStatus(Long applicationId,
                                                          UpdateApplicationStatusRequest request) {
        Company company = companyProfileService.getAuthenticatedCompany();

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(CompanyJobApplicationNotFoundException::new);

        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new CompanyJobApplicationNotFoundException();
        }

        if (!UpdateApplicationStatusRequest.isAllowedCompanyStatus(request.status())) {
            throw new InvalidCompanyApplicationStatusException();
        }

        application.setStatus(request.status());
        return jobApplicationMapper.toResponse(jobApplicationRepository.save(application));
    }
}
