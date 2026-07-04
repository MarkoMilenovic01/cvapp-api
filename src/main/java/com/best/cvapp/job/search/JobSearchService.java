package com.best.cvapp.job.search;

import com.best.cvapp.job.core.JobMapper;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.job.core.dto.JobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSearchService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public Page<JobResponse> searchJobs(JobSearchFilter filter, Pageable pageable) {
        return jobRepository.findAll(new JobSpecification(filter), pageable)
                .map(jobMapper::toResponse);
    }
}