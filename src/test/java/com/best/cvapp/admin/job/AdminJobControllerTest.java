package com.best.cvapp.admin.job;

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
public class AdminJobControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;
    private Long seededJobId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, job_applications, jobs, companies, users RESTART IDENTITY CASCADE");

        // seed admin
        jdbcTemplate.update(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'ADMIN', true, 'LOCAL')",
                "admin@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        // seed company user + company + job
        Long companyUserId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'COMPANY', true, 'LOCAL') RETURNING id",
                Long.class,
                "company@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        Long companyId = jdbcTemplate.queryForObject(
                "INSERT INTO companies (user_id, name) VALUES (?, ?) RETURNING id",
                Long.class,
                companyUserId, "BEST Nis"
        );

        seededJobId = jdbcTemplate.queryForObject(
                "INSERT INTO jobs (company_id, title, description, employment_type, work_mode, active) VALUES (?, ?, ?, 'INTERNSHIP', 'REMOTE', true) RETURNING id",
                Long.class,
                companyId, "Backend Intern", "Build REST APIs"
        );

        adminToken = extractAccessToken(login("admin@cvapp.com", "Test@1234"));
    }

    // ── GET /api/admin/jobs ───────────────────────────────────────────────────

    @Test
    void shouldGetAllJobs() throws Exception {
        mockMvc.perform(get("/api/admin/jobs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Intern"));
    }

    @Test
    void shouldRejectExcessiveJobPageSize() throws Exception {
        mockMvc.perform(get("/api/admin/jobs?size=101")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page size must not exceed 100"));
    }

    @Test
    void shouldRejectGetAllJobsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectGetAllJobsForNonAdmin() throws Exception {
        String companyToken = extractAccessToken(login("company@cvapp.com", "Test@1234"));

        mockMvc.perform(get("/api/admin/jobs")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/admin/jobs/{id} ──────────────────────────────────────────────

    @Test
    void shouldGetJobById() throws Exception {
        mockMvc.perform(get("/api/admin/jobs/" + seededJobId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Intern"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn404ForNonExistentJob() throws Exception {
        mockMvc.perform(get("/api/admin/jobs/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/admin/jobs/{id}/toggle ─────────────────────────────────────

    @Test
    void shouldToggleJobActive() throws Exception {
        mockMvc.perform(patch("/api/admin/jobs/" + seededJobId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // toggle back
        mockMvc.perform(patch("/api/admin/jobs/" + seededJobId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn404WhenTogglingNonExistentJob() throws Exception {
        mockMvc.perform(patch("/api/admin/jobs/99999/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectActivatingExpiredJob() throws Exception {
        jdbcTemplate.update(
                "UPDATE jobs SET active = false, deadline = CURRENT_DATE - 1 WHERE id = ?",
                seededJobId
        );

        mockMvc.perform(patch("/api/admin/jobs/" + seededJobId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("A job with a past deadline cannot be activated"));
    }

    // ── DELETE /api/admin/jobs/{id} ───────────────────────────────────────────

    @Test
    void shouldDeleteJob() throws Exception {
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
                seededJobId, applicantUserId, cvId
        );

        mockMvc.perform(delete("/api/admin/jobs/" + seededJobId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/jobs/" + seededJobId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        Integer remainingApplications = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM job_applications WHERE job_id = ?",
                Integer.class,
                seededJobId
        );
        org.assertj.core.api.Assertions.assertThat(remainingApplications).isZero();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentJob() throws Exception {
        mockMvc.perform(delete("/api/admin/jobs/99999")
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
