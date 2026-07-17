package com.best.cvapp.job.search;

import com.best.cvapp.job.core.dto.JobResponse;
import com.best.cvapp.job.core.JobPageSizeValidator;
import com.best.cvapp.job.search.dto.JobSearchFilter;
import jakarta.validation.Valid;
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
public class JobSearchController {

    private final JobSearchService jobSearchService;

    @GetMapping("/search")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @Valid @ModelAttribute JobSearchFilter filter,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        JobPageSizeValidator.validate(pageable);
        return ResponseEntity.ok(jobSearchService.searchJobs(filter, pageable));
    }
}
