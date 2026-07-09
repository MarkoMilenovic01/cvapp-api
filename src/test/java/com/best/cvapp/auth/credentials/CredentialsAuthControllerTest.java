package com.best.cvapp.auth.credentials;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.session.dto.RefreshTokenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CredentialsAuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, users RESTART IDENTITY CASCADE");
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "Test@1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldFailRegisterWithDuplicateEmail() throws Exception {
        register("test@best.com", "Test@1234");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "Test@1234"))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailRegisterWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("not-an-email", "Test@1234"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailRegisterWithShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "T@1"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailRegisterWithNoUppercase() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "test@1234"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailRegisterWithNoDigit() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "Test@abcd"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailRegisterWithNoSpecialChar() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "Test1234"))))
                .andExpect(status().isBadRequest());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void shouldLoginSuccessfully() throws Exception {
        register("test@best.com", "Test@1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("test@best.com", "Test@1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        register("test@best.com", "Test@1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("test@best.com", "Wrong@1234"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithNonExistentEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("nobody@best.com", "Test@1234"))))
                .andExpect(status().isUnauthorized());
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        String refreshToken = extractRefreshToken(register("test@best.com", "Test@1234"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldFailRefreshWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest("invalid-token"))))
                .andExpect(status().isUnauthorized());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        String refreshToken = extractRefreshToken(register("test@best.com", "Test@1234"));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailRefreshAfterLogout() throws Exception {
        String refreshToken = extractRefreshToken(register("test@best.com", "Test@1234"));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    // ── Authorization ─────────────────────────────────────────────────────────

    @Test
    void shouldRejectUserEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/cv"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUserEndpointWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUserEndpointWithWrongAuthScheme() throws Exception {
        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Basic some-random-value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessWithValidToken() throws Exception {
        String accessToken = extractAccessToken(register("test@best.com", "Test@1234"));

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectCompanyEndpointWithUserRole() throws Exception {
        String userToken = extractAccessToken(register("user@best.com", "Test@1234"));

        mockMvc.perform(get("/api/company/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAdminEndpointWithUserRole() throws Exception {
        String userToken = extractAccessToken(register("user@best.com", "Test@1234"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowUserEndpointWithValidUserToken() throws Exception {
        String userToken = extractAccessToken(register("user@best.com", "Test@1234"));

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowLoginEvenWithInvalidAuthorizationHeader() throws Exception {
        register("test@best.com", "Test@1234");

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("test@best.com", "Test@1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldAllowRegisterEvenWithInvalidAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "Test@1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MvcResult register(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private String extractRefreshToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("refreshToken").asText();
    }

    private RegisterRequest registerRequest(String email, String password) {
        return new RegisterRequest(email, password, password);
    }

    private LoginRequest loginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}