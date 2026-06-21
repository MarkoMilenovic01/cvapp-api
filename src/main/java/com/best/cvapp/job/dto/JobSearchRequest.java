package com.best.cvapp.job.dto;

import com.best.cvapp.job.EmploymentType;
import com.best.cvapp.job.WorkMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobSearchRequest {

    // Full-text search across title, description, requirements
    private String keyword;

    // Filter by location
    private String location;

    // Filter by employment type e.g. INTERNSHIP, STUDENT_WORK
    private EmploymentType employmentType;

    // Filter by work mode e.g. REMOTE, HYBRID, ONSITE
    private WorkMode workMode;

    // Filter by company name
    private String companyName;
}