package com.best.cvapp.admin.company;

import com.best.cvapp.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminCompanyControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;
    private Long seededCompanyId;
    private Long seededCompanyUserId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, companies, users RESTART IDENTITY CASCADE");

        // seed admin
        jdbcTemplate.update(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'ADMIN', true, 'LOCAL')",
                "admin@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        // seed company user + company
        seededCompanyUserId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'COMPANY', true, 'LOCAL') RETURNING id",
                Long.class,
                "company@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        seededCompanyId = jdbcTemplate.queryForObject(
                "INSERT INTO companies (user_id, name, description, website, industry) VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                seededCompanyUserId, "BEST Nis", "Tech company", "https://best.eu.org", "Education"
        );

        adminToken = extractAccessToken(login("admin@cvapp.com", "Test@1234"));
    }

    // ── GET /api/admin/companies ──────────────────────────────────────────────

    @Test
    void shouldGetAllCompanies() throws Exception {
        mockMvc.perform(get("/api/admin/companies")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("BEST Nis"));
    }

    @Test
    void shouldRejectExcessiveCompanyPageSize() throws Exception {
        mockMvc.perform(get("/api/admin/companies?size=101")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100"));
    }

    @Test
    void shouldRejectGetAllCompaniesWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/companies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectGetAllCompaniesForNonAdmin() throws Exception {
        String companyToken = extractAccessToken(login("company@cvapp.com", "Test@1234"));

        mockMvc.perform(get("/api/admin/companies")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/admin/companies/{id} ─────────────────────────────────────────

    @Test
    void shouldGetCompanyById() throws Exception {
        mockMvc.perform(get("/api/admin/companies/" + seededCompanyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BEST Nis"))
                .andExpect(jsonPath("$.industry").value("Education"));
    }

    @Test
    void shouldReturn404ForNonExistentCompany() throws Exception {
        mockMvc.perform(get("/api/admin/companies/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/admin/companies/{id} ──────────────────────────────────────

    @Test
    void shouldDeleteCompany() throws Exception {
        Long jobId = jdbcTemplate.queryForObject(
                "INSERT INTO jobs (company_id, title, description, employment_type, work_mode, active) " +
                        "VALUES (?, ?, ?, 'INTERNSHIP', 'REMOTE', true) RETURNING id",
                Long.class,
                seededCompanyId, "Backend Intern", "Build REST APIs"
        );
        Long applicantUserId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, role, enabled, provider) " +
                        "VALUES (?, ?, 'USER', true, 'LOCAL') RETURNING id",
                Long.class,
                "applicant@cvapp.com", passwordEncoder.encode("Test@1234")
        );
        Long cvId = jdbcTemplate.queryForObject(
                "INSERT INTO cvs (user_id, first_name, last_name) VALUES (?, ?, ?) RETURNING id",
                Long.class,
                applicantUserId, "Test", "Applicant"
        );
        jdbcTemplate.update(
                "INSERT INTO job_applications (job_id, user_id, cv_id, status) VALUES (?, ?, ?, 'APPLIED')",
                jobId, applicantUserId, cvId
        );

        login("company@cvapp.com", "Test@1234");

        mockMvc.perform(delete("/api/admin/companies/" + seededCompanyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/companies/" + seededCompanyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        Integer remainingUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class,
                seededCompanyUserId
        );
        Integer remainingRefreshTokens = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_tokens WHERE user_id = ?",
                Integer.class,
                seededCompanyUserId
        );
        Integer remainingJobs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM jobs WHERE company_id = ?",
                Integer.class,
                seededCompanyId
        );
        Integer remainingApplications = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM job_applications WHERE job_id = ?",
                Integer.class,
                jobId
        );

        org.assertj.core.api.Assertions.assertThat(remainingUsers).isZero();
        org.assertj.core.api.Assertions.assertThat(remainingRefreshTokens).isZero();
        org.assertj.core.api.Assertions.assertThat(remainingJobs).isZero();
        org.assertj.core.api.Assertions.assertThat(remainingApplications).isZero();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentCompany() throws Exception {
        mockMvc.perform(delete("/api/admin/companies/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.best.cvapp.auth.credentials.dto.LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
