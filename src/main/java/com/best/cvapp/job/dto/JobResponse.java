package com.best.cvapp.job.dto;

import com.best.cvapp.job.EmploymentType;
import com.best.cvapp.job.WorkMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class JobResponse {
    private Long id;

    private Long companyId;
    private String companyName;

    private String title;
    private String description;
    private String requirements;
    private String location;

    private EmploymentType employmentType;
    private WorkMode workMode;

    private LocalDate deadline;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}