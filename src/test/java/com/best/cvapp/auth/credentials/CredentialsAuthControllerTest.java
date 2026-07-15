package com.best.cvapp.auth.credentials;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.emailverification.dto.VerifyEmailRequest;
import com.best.cvapp.auth.session.dto.RefreshTokenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
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
        jdbcTemplate.execute(
                "TRUNCATE TABLE email_verification_tokens, refresh_tokens, users RESTART IDENTITY CASCADE");
    }



    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "Test@1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
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

    // ── Email Verification ───────────────────────────────────────────────────

    @Test
    void shouldVerifyEmailSuccessfully() throws Exception {
        register("test@best.com", "Test@1234");
        String token = fetchVerificationToken("test@best.com");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest(token))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailVerifyWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest("not-a-real-token"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailVerifyWithExpiredToken() throws Exception {
        register("test@best.com", "Test@1234");
        String token = fetchVerificationToken("test@best.com");

        jdbcTemplate.update("""
        UPDATE email_verification_tokens
        SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 minute'
        WHERE token = ?
        """, token);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest(token))))
                .andExpect(status().isBadRequest());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void shouldLoginSuccessfully() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

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
        registerAndVerify("test@best.com", "Test@1234");

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
        String refreshToken = extractRefreshToken(registerVerifyAndLogin("test@best.com", "Test@1234"));

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

    @Test
    void shouldFailRefreshWithExpiredRefreshToken() throws Exception {
        String refreshToken = extractRefreshToken(registerVerifyAndLogin("test@best.com", "Test@1234"));

        jdbcTemplate.update("""
        UPDATE refresh_tokens
        SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 minute'
        WHERE token = ?
        """, DigestUtils.sha256Hex(refreshToken));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        String refreshToken = extractRefreshToken(registerVerifyAndLogin("test@best.com", "Test@1234"));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailRefreshAfterLogout() throws Exception {
        String refreshToken = extractRefreshToken(registerVerifyAndLogin("test@best.com", "Test@1234"));

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
        String accessToken = extractAccessToken(registerVerifyAndLogin("test@best.com", "Test@1234"));

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectCompanyEndpointWithUserRole() throws Exception {
        String userToken = extractAccessToken(registerVerifyAndLogin("user@best.com", "Test@1234"));

        mockMvc.perform(get("/api/company/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAdminEndpointWithUserRole() throws Exception {
        String userToken = extractAccessToken(registerVerifyAndLogin("user@best.com", "Test@1234"));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowUserEndpointWithValidUserToken() throws Exception {
        String userToken = extractAccessToken(registerVerifyAndLogin("user@best.com", "Test@1234"));

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowLoginEvenWithInvalidAuthorizationHeader() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

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
                .andExpect(jsonPath("$.message").exists());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MvcResult register(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String fetchVerificationToken(String email) {
        return jdbcTemplate.queryForObject("""
            SELECT t.token FROM email_verification_tokens t
            JOIN users u ON u.id = t.user_id
            WHERE u.email = ?
            """, String.class, email);
    }

    private void verifyEmail(String email) throws Exception {
        String token = fetchVerificationToken(email);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest(token))))
                .andExpect(status().isNoContent());
    }

    private void registerAndVerify(String email, String password) throws Exception {
        register(email, password);
        verifyEmail(email);
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult registerVerifyAndLogin(String email, String password) throws Exception {
        registerAndVerify(email, password);
        return login(email, password);
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