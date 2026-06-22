package com.best.cvapp.job.company;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.job.application.JobApplication;
import com.best.cvapp.job.application.JobApplicationMapper;
import com.best.cvapp.job.application.JobApplicationRepository;
import com.best.cvapp.job.application.JobApplicationResponse;
import com.best.cvapp.job.application.UpdateApplicationStatusRequest;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobMapper;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.job.core.JobRequest;
import com.best.cvapp.job.core.JobResponse;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyJobService {

    private final JobRepository            jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyRepository        companyRepository;
    private final UserRepository           userRepository;
    private final JobMapper                jobMapper;
    private final JobApplicationMapper     jobApplicationMapper;

    // ── Job Management ────────────────────────────────────────────────────────

    @Transactional
    public JobResponse createJob(JobRequest request) {
        Company company = getAuthenticatedCompany();

        Job job = Job.builder()
                .company(company)
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .location(request.getLocation())
                .employmentType(request.getEmploymentType())
                .workMode(request.getWorkMode())
                .deadline(request.getDeadline())
                .active(true)
                .build();

        return jobMapper.toResponse(jobRepository.save(job));
    }

    public Page<JobResponse> getMyJobs(Pageable pageable) {
        Company company = getAuthenticatedCompany();
        return jobRepository.findByCompany(company, pageable)
                .map(jobMapper::toResponse);
    }

    public JobResponse getMyJobById(Long id) {
        Company company = getAuthenticatedCompany();
        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job not found"));
        return jobMapper.toResponse(job);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request) {
        Company company = getAuthenticatedCompany();

        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job not found"));

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setWorkMode(request.getWorkMode());
        job.setDeadline(request.getDeadline());

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        Company company = getAuthenticatedCompany();
        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job not found"));
        jobRepository.delete(job);
    }

    // ── Application Review ────────────────────────────────────────────────────

    public List<JobApplicationResponse> getApplicationsForJob(Long jobId) {
        Company company = getAuthenticatedCompany();
        Job job = jobRepository.findByIdAndCompany(jobId, company)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job not found"));
        return jobApplicationRepository.findByJobOrderByAppliedAtDesc(job).stream()
                .map(jobApplicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public JobApplicationResponse updateApplicationStatus(Long applicationId,
                                                          UpdateApplicationStatusRequest request) {
        Company company = getAuthenticatedCompany();

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found"));

        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }

        application.setStatus(request.getStatus());
        return jobApplicationMapper.toResponse(jobApplicationRepository.save(application));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Company getAuthenticatedCompany() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"));
    }
}