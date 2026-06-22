package com.best.cvapp.admin.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminStatsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, job_applications, jobs, cvs, companies, users RESTART IDENTITY CASCADE");

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

        jdbcTemplate.update(
                "INSERT INTO jobs (company_id, title, description, employment_type, work_mode, active) VALUES (?, ?, ?, 'INTERNSHIP', 'REMOTE', true)",
                companyId, "Backend Intern", "Build REST APIs"
        );

        jdbcTemplate.update(
                "INSERT INTO jobs (company_id, title, description, employment_type, work_mode, active) VALUES (?, ?, ?, 'INTERNSHIP', 'REMOTE', false)",
                companyId, "Inactive Job", "This job is inactive"
        );

        // seed regular user + cv
        Long userId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'USER', true, 'LOCAL') RETURNING id",
                Long.class,
                "user@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        jdbcTemplate.update(
                "INSERT INTO cvs (user_id, first_name, last_name) VALUES (?, ?, ?)",
                userId, "Marko", "Milenovic"
        );

        adminToken = extractAccessToken(login("admin@cvapp.com", "Test@1234"));
    }

    // ── GET /api/admin/stats ──────────────────────────────────────────────────

    @Test
    void shouldReturnCorrectStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))        // admin + company + user
                .andExpect(jsonPath("$.totalCompanies").value(1))
                .andExpect(jsonPath("$.totalCVs").value(1))
                .andExpect(jsonPath("$.totalJobs").value(2))
                .andExpect(jsonPath("$.activeJobs").value(1))
                .andExpect(jsonPath("$.inactiveJobs").value(1))
                .andExpect(jsonPath("$.totalApplications").value(0));
    }

    @Test
    void shouldRejectStatsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectStatsForNonAdmin() throws Exception {
        String userToken = extractAccessToken(login("user@cvapp.com", "Test@1234"));

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
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