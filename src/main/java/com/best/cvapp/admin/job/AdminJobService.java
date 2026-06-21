package com.best.cvapp.admin.job;

import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminJobService {

    private final JobRepository jobRepository;

    public Page<AdminJobResponse> getAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public AdminJobResponse getJobById(Long id) {
        return toResponse(findJob(id));
    }

    @Transactional
    public AdminJobResponse toggleActive(Long id) {
        Job job = findJob(id);
        job.setActive(!job.isActive());
        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.delete(findJob(id));
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job not found"));
    }

    private AdminJobResponse toResponse(Job job) {
        return new AdminJobResponse(
                job.getId(),
                job.getCompany().getId(),
                job.getCompany().getName(),
                job.getTitle(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getWorkMode(),
                job.getDeadline(),
                job.isActive(),
                job.getCreatedAt()
        );
    }
}