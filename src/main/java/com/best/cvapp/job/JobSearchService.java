package com.best.cvapp.job;

import com.best.cvapp.job.dto.JobResponse;
import com.best.cvapp.job.dto.JobSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final JobRepository jobRepository;

    public Page<JobResponse> searchJobs(JobSearchRequest request, Pageable pageable) {
        JobSpecification spec = new JobSpecification(request);

        return jobRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    private JobResponse toResponse(Job job) {
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