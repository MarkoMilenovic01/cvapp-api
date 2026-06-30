package com.best.cvapp.admin.company;

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
public class AdminCompanyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;
    private Long seededCompanyId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, companies, users RESTART IDENTITY CASCADE");

        // seed admin
        jdbcTemplate.update(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'ADMIN', true, 'LOCAL')",
                "admin@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        // seed company user + company
        Long companyUserId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'COMPANY', true, 'LOCAL') RETURNING id",
                Long.class,
                "company@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        seededCompanyId = jdbcTemplate.queryForObject(
                "INSERT INTO companies (user_id, name, description, website, industry) VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                companyUserId, "BEST Nis", "Tech company", "https://best.eu.org", "Education"
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
        mockMvc.perform(delete("/api/admin/companies/" + seededCompanyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/companies/" + seededCompanyId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
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