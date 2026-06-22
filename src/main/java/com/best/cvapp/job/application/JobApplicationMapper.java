package com.best.cvapp.job.application;

import com.best.cvapp.cv.profile.CV;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {

    public JobApplicationResponse toResponse(JobApplication application) {
        CV cv = application.getCv();
        return new JobApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getCompany().getId(),
                application.getJob().getCompany().getName(),
                application.getUser().getId(),
                cv.getId(),
                cv.getFirstName(),
                cv.getLastName(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt()
        );
    }
}