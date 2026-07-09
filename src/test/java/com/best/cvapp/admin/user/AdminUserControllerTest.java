package com.best.cvapp.admin.user;

import com.best.cvapp.admin.user.dto.ChangeRoleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
public class AdminUserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;
    private Long seededUserId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, users RESTART IDENTITY CASCADE");

        // seed admin
        jdbcTemplate.update(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'ADMIN', true, 'LOCAL')",
                "admin@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        // seed a regular user to manage
        seededUserId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password, role, enabled, provider) VALUES (?, ?, 'USER', true, 'LOCAL') RETURNING id",
                Long.class,
                "user@cvapp.com", passwordEncoder.encode("Test@1234")
        );

        adminToken = extractAccessToken(login("admin@cvapp.com", "Test@1234"));
    }

    // ── GET /api/admin/users ──────────────────────────────────────────────────

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldRejectGetAllUsersWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectGetAllUsersForNonAdmin() throws Exception {
        String userToken = extractAccessToken(login("user@cvapp.com", "Test@1234"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/admin/users/{id} ─────────────────────────────────────────────

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + seededUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@cvapp.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void shouldReturn404ForNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/admin/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/admin/users/{id}/toggle ───────────────────────────────────

    @Test
    void shouldToggleUserEnabled() throws Exception {
        mockMvc.perform(patch("/api/admin/users/" + seededUserId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        // toggle back
        mockMvc.perform(patch("/api/admin/users/" + seededUserId + "/toggle")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    // ── PATCH /api/admin/users/{id}/role ─────────────────────────────────────

    @Test
    void shouldChangeUserRole() throws Exception {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setRole(com.best.cvapp.user.Role.ADMIN);

        mockMvc.perform(patch("/api/admin/users/" + seededUserId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldReturn404WhenChangingRoleOfNonExistentUser() throws Exception {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setRole(com.best.cvapp.user.Role.ADMIN);

        mockMvc.perform(patch("/api/admin/users/99999/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/admin/users/{id} ─────────────────────────────────────────

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + seededUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/users/" + seededUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
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