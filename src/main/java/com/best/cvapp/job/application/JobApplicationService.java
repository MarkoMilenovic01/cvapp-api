package com.best.cvapp.job.application;

import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.job.application.dto.JobApplicationResponse;
import com.best.cvapp.job.application.exception.ApplicantCVNotFoundException;
import com.best.cvapp.job.application.exception.ApplicantUserNotFoundException;
import com.best.cvapp.job.application.exception.ApplicationJobNotFoundException;
import com.best.cvapp.job.application.exception.DuplicateJobApplicationException;
import com.best.cvapp.job.application.exception.JobApplicationNotFoundException;
import com.best.cvapp.job.application.exception.JobDeadlineExpiredException;
import com.best.cvapp.job.core.ApplicationStatus;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles job applications for authenticated users.
 *
 * Flow:
 * 1. Load the authenticated user and requested active job.
 * 2. Validate the application deadline and the user's CV.
 * 3. Reject duplicate applications and create a new application.
 * 4. List the authenticated user's applications in newest-first order.
 * 5. Verify ownership before withdrawing an application.
 */
@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository            jobRepository;
    private final CVRepository             cvRepository;
    private final UserRepository           userRepository;
    private final JobApplicationMapper     jobApplicationMapper;

    @Transactional
    public JobApplicationResponse applyToJob(Long jobId) {
        User user = getAuthenticatedUser();

        Job job = jobRepository.findByIdAndActiveTrue(jobId)
                .orElseThrow(ApplicationJobNotFoundException::new);

        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
            throw new JobDeadlineExpiredException();
        }

        CV cv = cvRepository.findByUser(user)
                .orElseThrow(ApplicantCVNotFoundException::new);

        if (jobApplicationRepository.existsByJobAndUser(job, user)) {
            throw new DuplicateJobApplicationException();
        }

        JobApplication application = JobApplication.builder()
                .job(job)
                .user(user)
                .cv(cv)
                .status(ApplicationStatus.APPLIED)
                .build();

        try {
            return jobApplicationMapper.toResponse(
                    jobApplicationRepository.saveAndFlush(application)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateJobApplicationException();
        }
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getMyApplications() {
        User user = getAuthenticatedUser();
        return jobApplicationRepository.findByUserOrderByAppliedAtDesc(user).stream()
                .map(jobApplicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public void withdrawApplication(Long applicationId) {
        User user = getAuthenticatedUser();
        JobApplication application = jobApplicationRepository.findByIdAndUser(applicationId, user)
                .orElseThrow(JobApplicationNotFoundException::new);
        jobApplicationRepository.delete(application);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(ApplicantUserNotFoundException::new);
    }
}
