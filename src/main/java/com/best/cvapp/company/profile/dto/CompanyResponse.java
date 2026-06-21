package com.best.cvapp.company.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String description;
    private String website;
    private String industry;
    private String photoUrl;
    private LocalDateTime createdAt;
}