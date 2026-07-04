package com.best.cvapp.job.core.dto;

import com.best.cvapp.job.core.EmploymentType;
import com.best.cvapp.job.core.WorkMode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class JobRequest {
    private String title;
    private String description;
    private String requirements;
    private String location;
    private EmploymentType employmentType;
    private WorkMode workMode;
    private LocalDate deadline;
}