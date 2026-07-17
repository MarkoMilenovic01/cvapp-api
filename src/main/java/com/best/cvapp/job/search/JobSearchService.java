package com.best.cvapp.job.search;

import com.best.cvapp.job.core.JobMapper;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.job.core.dto.JobResponse;
import com.best.cvapp.job.search.dto.JobSearchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles filtered job searches for authenticated users.
 *
 * Flow:
 * 1. Build a job specification from the submitted search filters.
 * 2. Restrict results to active jobs with valid deadlines.
 * 3. Apply keyword, location, work-mode, employment-type, and company filters.
 * 4. Search using the requested pagination.
 * 5. Map matching jobs to their public responses.
 */
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
