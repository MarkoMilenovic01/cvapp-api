package com.best.cvapp.job.dto;

import com.best.cvapp.job.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class JobApplicationResponse {
    private Long id;

    private Long jobId;
    private String jobTitle;

    private Long companyId;
    private String companyName;

    private Long userId;
    private Long cvId;
    private String applicantFirstName;
    private String applicantLastName;

    private ApplicationStatus status;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}