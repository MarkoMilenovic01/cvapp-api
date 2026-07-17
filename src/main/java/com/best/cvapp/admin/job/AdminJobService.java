package com.best.cvapp.admin.job;

import com.best.cvapp.admin.job.dto.AdminJobResponse;
import com.best.cvapp.admin.job.exception.AdminCannotActivateExpiredJobException;
import com.best.cvapp.admin.job.exception.AdminJobNotFoundException;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

        if (!job.isActive()
                && job.getDeadline() != null
                && job.getDeadline().isBefore(LocalDate.now())) {
            throw new AdminCannotActivateExpiredJobException();
        }

        job.setActive(!job.isActive());
        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.delete(findJob(id));
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(AdminJobNotFoundException::new);
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
