package com.best.cvapp.job.application;

import com.best.cvapp.job.core.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateApplicationStatusRequest {
    private ApplicationStatus status;
}