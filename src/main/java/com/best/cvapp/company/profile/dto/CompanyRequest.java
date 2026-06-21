package com.best.cvapp.company.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequest {
    private String name;
    private String description;
    private String website;
    private String industry;
}