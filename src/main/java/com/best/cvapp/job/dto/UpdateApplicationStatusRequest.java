package com.best.cvapp.job.dto;

import com.best.cvapp.job.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateApplicationStatusRequest {
    private ApplicationStatus status;
}