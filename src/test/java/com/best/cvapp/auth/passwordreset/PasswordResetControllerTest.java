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
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordResetControllerTest extends AbstractIntegrationTest {

    private static final String AUTH_URL = "/api/auth";
    private static final String TEST_EMAIL = "test@best.com";
    private static final String TEST_PASSWORD = "Test@1234";
    private static final String NEW_PASSWORD = "NewPass@1234";

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
        registerAndVerify(TEST_EMAIL, TEST_PASSWORD);

        mockMvc.perform(post(AUTH_URL + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest(TEST_EMAIL))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404ForNonExistentEmail() throws Exception {
        mockMvc.perform(post(AUTH_URL + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest("nobody@best.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailForgotPasswordWithBlankEmail() throws Exception {
        mockMvc.perform(post(AUTH_URL + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest(""))))
                .andExpect(status().isBadRequest());
    }

    // ── Reset Password ────────────────────────────────────────────────────────

    @Test
    void shouldResetPasswordSuccessfully() throws Exception {
        String token = prepareResetToken();
        resetPassword(token, NEW_PASSWORD, NEW_PASSWORD)
                .andExpect(status().isNoContent());

        mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(TEST_EMAIL, NEW_PASSWORD))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailLoginWithOldPasswordAfterReset() throws Exception {
        String token = prepareResetToken();
        resetPassword(token, NEW_PASSWORD, NEW_PASSWORD)
                .andExpect(status().isNoContent());

        mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailResetWithInvalidToken() throws Exception {
        resetPassword("invalid-token", NEW_PASSWORD, NEW_PASSWORD)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailResetWithExpiredToken() throws Exception {
        String token = prepareResetToken();

        jdbcTemplate.update("""
            UPDATE password_reset_tokens
            SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 hour'
            WHERE email = ?
            """, TEST_EMAIL);

        resetPassword(token, NEW_PASSWORD, NEW_PASSWORD)
                .andExpect(status().isGone());
    }

    @Test
    void shouldFailResetWhenPasswordsDoNotMatch() throws Exception {
        String token = prepareResetToken();

        resetPassword(token, NEW_PASSWORD, "Different@1234")
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailResetTokenUsedTwice() throws Exception {
        String token = prepareResetToken();

        resetPassword(token, NEW_PASSWORD, NEW_PASSWORD)
                .andExpect(status().isNoContent());

        resetPassword(token, "Another@1234", "Another@1234")
                .andExpect(status().isGone());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post(AUTH_URL + "/register")
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

        mockMvc.perform(post(AUTH_URL + "/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest(token))))
                .andExpect(status().isNoContent());
    }

    private String getVerificationToken(String email) {
        return storeKnownVerificationToken(jdbcTemplate, email);
    }

    private void requestReset(String email) throws Exception {
        mockMvc.perform(post(AUTH_URL + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new ForgotPasswordRequest(email))))
                .andExpect(status().isNoContent());
    }

    private String getResetToken(String email) {
        return storeKnownPasswordResetToken(jdbcTemplate, email);
    }

    private String prepareResetToken() throws Exception {
        registerAndVerify(TEST_EMAIL, TEST_PASSWORD);
        requestReset(TEST_EMAIL);
        return getResetToken(TEST_EMAIL);
    }

    private ResultActions resetPassword(
            String token,
            String password,
            String confirmPassword
    ) throws Exception {
        return mockMvc.perform(post(AUTH_URL + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new PasswordResetRequest(token, password, confirmPassword))));
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
