package com.best.cvapp.job.application;

import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.job.application.dto.JobApplicationResponse;
import com.best.cvapp.job.core.ApplicationStatus;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobRepository;
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
    private final JobRepository            jobRepository;
    private final CVRepository             cvRepository;
    private final UserRepository           userRepository;
    private final JobApplicationMapper     jobApplicationMapper;

    @Transactional
    public JobApplicationResponse applyToJob(Long jobId) {
        User user = getAuthenticatedUser();

        Job job = jobRepository.findByIdAndActiveTrue(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job not found"));

        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Job application deadline has passed");
        }

        CV cv = cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CV not found — create your CV first"));

        if (jobApplicationRepository.existsByJobAndUser(job, user)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .job(job)
                .user(user)
                .cv(cv)
                .status(ApplicationStatus.APPLIED)
                .build();

        return jobApplicationMapper.toResponse(jobApplicationRepository.save(application));
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found"));
        jobApplicationRepository.delete(application);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }
}