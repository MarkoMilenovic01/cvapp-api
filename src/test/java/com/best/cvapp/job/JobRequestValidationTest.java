package com.best.cvapp.job;

import com.best.cvapp.job.application.dto.UpdateApplicationStatusRequest;
import com.best.cvapp.job.company.dto.UpdateJobActiveRequest;
import com.best.cvapp.job.core.ApplicationStatus;
import com.best.cvapp.job.core.EmploymentType;
import com.best.cvapp.job.core.WorkMode;
import com.best.cvapp.job.core.dto.JobRequest;
import com.best.cvapp.job.search.dto.JobSearchFilter;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class JobRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectMissingRequiredJobFields() {
        JobRequest request = new JobRequest("   ", "   ", null, null, null, null, null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "description", "employmentType", "workMode");
    }

    @Test
    void shouldRejectOversizedJobTextAndPastDeadline() {
        JobRequest request = new JobRequest(
                "a".repeat(256),
                "a".repeat(10001),
                "a".repeat(10001),
                "a".repeat(256),
                EmploymentType.FULL_TIME,
                WorkMode.REMOTE,
                LocalDate.now().minusDays(1)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "title", "description", "requirements", "location", "deadline"
                );
    }

    @Test
    void shouldAcceptCompanyManagedApplicationStatuses() {
        for (ApplicationStatus status : new ApplicationStatus[]{
                ApplicationStatus.REVIEWED,
                ApplicationStatus.SHORTLISTED,
                ApplicationStatus.CONTACTED,
                ApplicationStatus.REJECTED,
                ApplicationStatus.ACCEPTED
        }) {
            assertThat(validator.validate(new UpdateApplicationStatusRequest(status))).isEmpty();
        }
    }

    @Test
    void shouldRejectApplicantManagedApplicationStatuses() {
        assertThat(validator.validate(
                new UpdateApplicationStatusRequest(ApplicationStatus.APPLIED)
        )).isNotEmpty();
        assertThat(validator.validate(
                new UpdateApplicationStatusRequest(ApplicationStatus.WITHDRAWN)
        )).isNotEmpty();
    }

    @Test
    void shouldRejectMissingApplicationStatus() {
        assertThat(validator.validate(new UpdateApplicationStatusRequest(null))).isNotEmpty();
    }

    @Test
    void shouldRejectMissingActiveState() {
        assertThat(validator.validate(new UpdateJobActiveRequest(null))).isNotEmpty();
    }

    @Test
    void shouldRejectOversizedSearchFilters() {
        JobSearchFilter filter = new JobSearchFilter(
                "a".repeat(201),
                "a".repeat(256),
                null,
                null,
                "a".repeat(256)
        );

        assertThat(validator.validate(filter))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("keyword", "location", "companyName");
    }
}
