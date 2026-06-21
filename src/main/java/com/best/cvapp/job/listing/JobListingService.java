package com.best.cvapp.job.listing;

import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobMapper;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.job.core.JobResponse;
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
public class JobListingService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByActiveTrue(pageable)
                .map(jobMapper::toResponse);
    }

    public JobResponse getActiveJobById(Long id) {
        Job job = jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Job not found"));
        return jobMapper.toResponse(job);
    }
}