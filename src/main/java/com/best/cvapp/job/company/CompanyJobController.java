package com.best.cvapp.job.company;

import com.best.cvapp.job.application.JobApplicationResponse;
import com.best.cvapp.job.application.UpdateApplicationStatusRequest;
import com.best.cvapp.job.core.JobRequest;
import com.best.cvapp.job.core.JobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
public class CompanyJobController {

    private final CompanyJobService companyJobService;

    // ── Job Management ────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyJobService.createJob(request));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(companyJobService.getMyJobs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getMyJobById(@PathVariable Long id) {
        return ResponseEntity.ok(companyJobService.getMyJobById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @RequestBody JobRequest request) {
        return ResponseEntity.ok(companyJobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        companyJobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    // ── Application Review ────────────────────────────────────────────────────

    @GetMapping("/{jobId}/applications")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId) {
        return ResponseEntity.ok(companyJobService.getApplicationsForJob(jobId));
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateApplicationStatusRequest request) {
        return ResponseEntity.ok(companyJobService.updateApplicationStatus(applicationId, request));
    }
}