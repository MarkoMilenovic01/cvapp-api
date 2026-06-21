package com.best.cvapp.job;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.job.dto.JobRequest;
import com.best.cvapp.job.dto.JobResponse;
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

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

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

        return mapToResponse(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getMyJobs(Pageable pageable) {
        Company company = getAuthenticatedCompany();

        return jobRepository.findByCompany(company, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getMyJobById(Long id) {
        Company company = getAuthenticatedCompany();

        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        return mapToResponse(job);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request) {
        Company company = getAuthenticatedCompany();

        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setWorkMode(request.getWorkMode());
        job.setDeadline(request.getDeadline());

        return mapToResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        Company company = getAuthenticatedCompany();

        Job job = jobRepository.findByIdAndCompany(id, company)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        jobRepository.delete(job);
    }


    @Transactional(readOnly = true)
    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse getActiveJobById(Long id) {
        Job job = jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        return mapToResponse(job);
    }

    private Company getAuthenticatedCompany() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }

    private JobResponse mapToResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getCompany().getId(),
                job.getCompany().getName(),
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getWorkMode(),
                job.getDeadline(),
                job.isActive(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}