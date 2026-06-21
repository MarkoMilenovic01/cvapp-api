package com.best.cvapp.company.history.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CVViewResponse {
    private Long cvId;
    private String firstName;
    private String lastName;
    private LocalDateTime viewedAt;
}