package com.best.cvapp.job.listing;

import com.best.cvapp.job.core.dto.JobResponse;
import com.best.cvapp.job.core.JobPageSizeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CompanyJobListingController {

    private final JobListingService jobListingService;

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getActiveJobsByCompany(
            @PathVariable Long companyId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        JobPageSizeValidator.validate(pageable);
        return ResponseEntity.ok(
                jobListingService.getActiveJobsByCompany(companyId, pageable)
        );
    }
}
