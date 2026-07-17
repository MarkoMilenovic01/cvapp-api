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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class JobListingController {

    private final JobListingService jobListingService;

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllActiveJobs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        JobPageSizeValidator.validate(pageable);
        return ResponseEntity.ok(jobListingService.getAllActiveJobs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getActiveJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobListingService.getActiveJobById(id));
    }
}
