package com.best.cvapp.job.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<JobApplicationResponse> applyToJob(@PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobApplicationService.applyToJob(jobId));
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(jobApplicationService.getMyApplications());
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long applicationId) {
        jobApplicationService.withdrawApplication(applicationId);
        return ResponseEntity.noContent().build();
    }
}