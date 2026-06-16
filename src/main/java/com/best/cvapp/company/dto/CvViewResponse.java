package com.best.cvapp.company.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CvViewResponse {
    private Long cvId;
    private String firstName;
    private String lastName;
    private LocalDateTime viewedAt;
}