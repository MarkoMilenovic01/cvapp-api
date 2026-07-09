package com.best.cvapp.job.search.dto;

import com.best.cvapp.job.core.EmploymentType;
import com.best.cvapp.job.core.WorkMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobSearchFilter {
    private String keyword;
    private String location;
    private WorkMode workMode;
    private EmploymentType employmentType;
    private String companyName;
}