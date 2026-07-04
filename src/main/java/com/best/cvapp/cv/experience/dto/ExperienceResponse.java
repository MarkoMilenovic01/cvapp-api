package com.best.cvapp.cv.experience.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ExperienceResponse {
    private Long id;
    private String companyName;
    private String position;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
}