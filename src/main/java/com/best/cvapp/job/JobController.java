package com.best.cvapp.job;

import com.best.cvapp.job.dto.JobApplicationResponse;
import com.best.cvapp.job.dto.JobRequest;
import com.best.cvapp.job.dto.JobResponse;
import com.best.cvapp.job.dto.UpdateApplicationStatusRequest;
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
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobApplicationService jobApplicationService;

    // --- Company Job Management ---

    @PostMapping("/api/company/jobs")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(request));
    }

    @GetMapping("/api/company/jobs")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<JobResponse>> getMyCompanyJobs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(jobService.getMyJobs(pageable));
    }

    @GetMapping("/api/company/jobs/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobResponse> getMyCompanyJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getMyJobById(id));
    }

    @PutMapping("/api/company/jobs/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/api/company/jobs/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    // --- User Job Browsing ---

    @GetMapping("/api/jobs")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<JobResponse>> getAllActiveJobs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(jobService.getAllActiveJobs(pageable));
    }

    @GetMapping("/api/jobs/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<JobResponse> getActiveJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getActiveJobById(id));
    }

    // --- User Job Applications ---

    @PostMapping("/api/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<JobApplicationResponse> applyToJob(@PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobApplicationService.applyToJob(jobId));
    }

    @GetMapping("/api/user/applications")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(jobApplicationService.getMyApplications());
    }

    @DeleteMapping("/api/user/applications/{applicationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long applicationId) {
        jobApplicationService.withdrawApplication(applicationId);
        return ResponseEntity.noContent().build();
    }

    // --- Company Application Review ---

    @GetMapping("/api/company/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsForCompanyJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsForCompanyJob(jobId));
    }

    @PatchMapping("/api/company/applications/{applicationId}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateApplicationStatusRequest request) {
        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(applicationId, request));
    }
}