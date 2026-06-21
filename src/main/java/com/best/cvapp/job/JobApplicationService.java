package com.best.cvapp.job;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.cv.CV;
import com.best.cvapp.cv.CVRepository;
import com.best.cvapp.job.dto.JobApplicationResponse;
import com.best.cvapp.job.dto.UpdateApplicationStatusRequest;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public JobApplicationResponse applyToJob(Long jobId) {
        User user = getAuthenticatedUser();

        Job job = jobRepository.findByIdAndActiveTrue(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job application deadline has passed");
        }

        CV cv = cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));

        if (jobApplicationRepository.existsByJobAndUser(job, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .job(job)
                .user(user)
                .cv(cv)
                .status(ApplicationStatus.APPLIED)
                .build();

        return mapToResponse(jobApplicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getMyApplications() {
        User user = getAuthenticatedUser();

        return jobApplicationRepository.findByUserOrderByAppliedAtDesc(user).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void withdrawApplication(Long applicationId) {
        User user = getAuthenticatedUser();

        JobApplication application = jobApplicationRepository.findByIdAndUser(applicationId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        jobApplicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getApplicationsForCompanyJob(Long jobId) {
        Company company = getAuthenticatedCompany();

        Job job = jobRepository.findByIdAndCompany(jobId, company)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        return jobApplicationRepository.findByJobOrderByAppliedAtDesc(job).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public JobApplicationResponse updateApplicationStatus(Long applicationId, UpdateApplicationStatusRequest request) {
        Company company = getAuthenticatedCompany();

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }

        application.setStatus(request.getStatus());

        return mapToResponse(jobApplicationRepository.save(application));
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Company getAuthenticatedCompany() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return companyRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }

    private JobApplicationResponse mapToResponse(JobApplication application) {
        CV cv = application.getCv();

        return new JobApplicationResponse(
                application.getId(),

                application.getJob().getId(),
                application.getJob().getTitle(),

                application.getJob().getCompany().getId(),
                application.getJob().getCompany().getName(),

                application.getUser().getId(),
                cv.getId(),
                cv.getFirstName(),
                cv.getLastName(),

                application.getStatus(),

                application.getAppliedAt(),
                application.getUpdatedAt()
        );
    }
}