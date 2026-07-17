package com.best.cvapp.job.listing;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.core.JobMapper;
import com.best.cvapp.job.core.JobRepository;
import com.best.cvapp.job.core.dto.JobResponse;
import com.best.cvapp.job.listing.exception.JobListingCompanyNotFoundException;
import com.best.cvapp.job.listing.exception.ListedJobNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Handles user-facing job listings.
 *
 * Flow:
 * 1. Load jobs that are active and have not passed their deadline.
 * 2. Return all visible jobs using the requested pagination.
 * 3. Load a visible job by its identifier.
 * 4. Validate a requested company before listing its visible jobs.
 * 5. Map jobs to their public responses.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobListingService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final CompanyRepository companyRepository;

    public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findVisibleJobs(LocalDate.now(), pageable)
                .map(jobMapper::toResponse);
    }

    public JobResponse getActiveJobById(Long id) {
        Job job = jobRepository.findVisibleJobById(id, LocalDate.now())
                .orElseThrow(ListedJobNotFoundException::new);
        return jobMapper.toResponse(job);
    }


    public Page<JobResponse> getActiveJobsByCompany(Long companyId, Pageable pageable) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(JobListingCompanyNotFoundException::new);

        return jobRepository.findVisibleJobsByCompany(company, LocalDate.now(), pageable)
                .map(jobMapper::toResponse);
    }
}
