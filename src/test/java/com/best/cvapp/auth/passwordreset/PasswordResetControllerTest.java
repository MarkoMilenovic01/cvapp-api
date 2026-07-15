package com.best.cvapp.auth.passwordreset;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.emailverification.dto.VerifyEmailRequest;
import com.best.cvapp.auth.passwordreset.dto.ForgotPasswordRequest;
import com.best.cvapp.auth.passwordreset.dto.PasswordResetRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PasswordResetControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
            TRUNCATE TABLE password_reset_tokens, email_verification_tokens, refresh_tokens, users
            RESTART IDENTITY CASCADE
            """);
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    @Test
    void shouldSendResetEmailForExistingUser() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest("test@best.com"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404ForNonExistentEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest("nobody@best.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailForgotPasswordWithBlankEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest(""))))
                .andExpect(status().isBadRequest());
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Test
    void shouldResetPasswordSuccessfully() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

        requestReset("test@best.com");

        String token = getResetToken("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new PasswordResetRequest(
                                token,
                                "NewPass@1234",
                                "NewPass@1234"
                        ))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("test@best.com", "NewPass@1234"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailLoginWithOldPasswordAfterReset() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

        requestReset("test@best.com");

        String token = getResetToken("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new PasswordResetRequest(
                                token,
                                "NewPass@1234",
                                "NewPass@1234"
                        ))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("test@best.com", "Test@1234"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailResetWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new PasswordResetRequest(
                                "invalid-token",
                                "NewPass@1234",
                                "NewPass@1234"
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailResetWithExpiredToken() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

        requestReset("test@best.com");

        jdbcTemplate.update("""
            UPDATE password_reset_tokens
            SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 hour'
            WHERE email = ?
            """, "test@best.com");

        String token = getResetToken("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new PasswordResetRequest(
                                token,
                                "NewPass@1234",
                                "NewPass@1234"
                        ))))
                .andExpect(status().isGone());
    }

    @Test
    void shouldFailResetWhenPasswordsDoNotMatch() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

        requestReset("test@best.com");

        String token = getResetToken("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new PasswordResetRequest(
                                token,
                                "NewPass@1234",
                                "Different@1234"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailResetTokenUsedTwice() throws Exception {
        registerAndVerify("test@best.com", "Test@1234");

        requestReset("test@best.com");

        String token = getResetToken("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new PasswordResetRequest(
                                token,
                                "NewPass@1234",
                                "NewPass@1234"
                        ))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new PasswordResetRequest(
                                token,
                                "Another@1234",
                                "Another@1234"
                        ))))
                .andExpect(status().isGone());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RegisterRequest(email, password, password))))
                .andExpect(status().isOk());
    }

    private void registerAndVerify(String email, String password) throws Exception {
        register(email, password);
        verifyEmail(email);
    }

    private void verifyEmail(String email) throws Exception {
        String token = getVerificationToken(email);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest(token))))
                .andExpect(status().isNoContent());
    }

    private String getVerificationToken(String email) {
        return jdbcTemplate.queryForObject("""
            SELECT t.token
            FROM email_verification_tokens t
            JOIN users u ON u.id = t.user_id
            WHERE u.email = ?
            """, String.class, email);
    }

    private void requestReset(String email) throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest(email))))
                .andExpect(status().isNoContent());
    }

    private String getResetToken(String email) {
        return jdbcTemplate.queryForObject("""
            SELECT token
            FROM password_reset_tokens
            WHERE email = ?
            """, String.class, email);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}