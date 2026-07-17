package com.best.cvapp.cv;

import com.best.cvapp.cv.experience.ExperienceType;
import com.best.cvapp.cv.experience.dto.ExperienceRequest;
import com.best.cvapp.cv.profile.dto.CVRequest;
import com.best.cvapp.cv.project.dto.ProjectRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CVTextLengthValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectSummaryLongerThan5000Characters() {
        CVRequest request = new CVRequest(
                null, null, null, null, "a".repeat(5001), null, null
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("summary"));
    }

    @Test
    void shouldRejectExperienceDescriptionLongerThan5000Characters() {
        ExperienceRequest request = new ExperienceRequest(
                "Company", "Developer", ExperienceType.FULL_TIME,
                "a".repeat(5001), null, null, false
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("description"));
    }

    @Test
    void shouldRejectProjectDescriptionLongerThan5000Characters() {
        ProjectRequest request = new ProjectRequest(
                "Project", "a".repeat(5001), null, null, null, null, false
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("description"));
    }
}
