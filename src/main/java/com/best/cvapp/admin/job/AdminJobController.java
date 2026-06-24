package com.best.cvapp.admin.job;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobController {

    private final AdminJobService adminJobService;

    @GetMapping
    public ResponseEntity<Page<AdminJobResponse>> getAllJobs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(adminJobService.getAllJobs(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminJobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(adminJobService.getJobById(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<AdminJobResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(adminJobService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        adminJobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}