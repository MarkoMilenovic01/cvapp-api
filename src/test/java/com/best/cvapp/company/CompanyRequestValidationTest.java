package com.best.cvapp.company;

import com.best.cvapp.company.cv.dto.CVSearchRequest;
import com.best.cvapp.company.profile.dto.CompanyRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectBlankCompanyName() {
        CompanyRequest request = new CompanyRequest("   ", null, null, null);

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name"));
    }

    @Test
    void shouldRejectOversizedCompanyFields() {
        CompanyRequest request = new CompanyRequest(
                "a".repeat(256),
                "a".repeat(5001),
                "https://example.com/" + "a".repeat(240),
                "a".repeat(101)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "description", "website", "industry");
    }

    @Test
    void shouldRejectUnsafeWebsiteScheme() {
        CompanyRequest request = new CompanyRequest(
                "Company", null, "javascript:alert(1)", null
        );

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("website"));
    }

    @Test
    void shouldAcceptHttpAndHttpsWebsites() {
        CompanyRequest http = new CompanyRequest("Company", null, "http://example.com", null);
        CompanyRequest https = new CompanyRequest("Company", null, "https://example.com", null);

        assertThat(validator.validate(http)).isEmpty();
        assertThat(validator.validate(https)).isEmpty();
    }

    @Test
    void shouldRejectOversizedSearchFilters() {
        CVSearchRequest request = new CVSearchRequest(
                "a".repeat(201),
                "a".repeat(101),
                "a".repeat(256)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("keyword", "skill", "location");
    }
}
