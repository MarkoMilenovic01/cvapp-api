package com.best.cvapp;

import com.best.cvapp.auth.dto.LoginRequest;
import com.best.cvapp.auth.dto.RegisterRequest;
import com.best.cvapp.auth.token.passwordreset.ForgotPasswordRequest;
import com.best.cvapp.auth.token.passwordreset.PasswordResetRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PasswordResetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                password_reset_tokens,
                refresh_tokens,
                users
                RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void shouldSendResetEmailForExistingUser() throws Exception {
        register("test@best.com", "123456");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest("test@best.com"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404ForNonExistentEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest("nobody@best.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailForgotPasswordWithBlankEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldResetPasswordSuccessfully() throws Exception {
        register("test@best.com", "123456");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest("test@best.com"))))
                .andExpect(status().isNoContent());

        String token = getResetTokenForEmail("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resetPasswordRequest(token, "newpassword123", "newpassword123"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("test@best.com", "newpassword123"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailLoginWithOldPasswordAfterReset() throws Exception {
        register("test@best.com", "123456");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest("test@best.com"))))
                .andExpect(status().isNoContent());

        String token = getResetTokenForEmail("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resetPasswordRequest(token, "newpassword123", "newpassword123"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("test@best.com", "123456"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailResetWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resetPasswordRequest("invalid-token", "newpassword123", "newpassword123"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailResetWithExpiredToken() throws Exception {
        register("test@best.com", "123456");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest("test@best.com"))))
                .andExpect(status().isNoContent());

        jdbcTemplate.update("""
                UPDATE password_reset_tokens
                SET expires_at = NOW() - INTERVAL '1 hour'
                WHERE email = ?
                """, "test@best.com");

        String token = getResetTokenForEmail("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resetPasswordRequest(token, "newpassword123", "newpassword123"))))
                .andExpect(status().isGone());
    }

    @Test
    void shouldFailResetWhenPasswordsDoNotMatch() throws Exception {
        register("test@best.com", "123456");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest("test@best.com"))))
                .andExpect(status().isNoContent());

        String token = getResetTokenForEmail("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resetPasswordRequest(token, "newpassword123", "differentpassword"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailResetTokenUsedTwice() throws Exception {
        register("test@best.com", "123456");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(forgotPasswordRequest("test@best.com"))))
                .andExpect(status().isNoContent());

        String token = getResetTokenForEmail("test@best.com");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resetPasswordRequest(token, "newpassword123", "newpassword123"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resetPasswordRequest(token, "anotherpassword123", "anotherpassword123"))))
                .andExpect(status().isGone());
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest(email, password))))
                .andExpect(status().isOk());
    }

    private String getResetTokenForEmail(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT token FROM password_reset_tokens WHERE email = ?",
                String.class,
                email
        );
    }

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private ForgotPasswordRequest forgotPasswordRequest(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);
        return request;
    }

    private PasswordResetRequest resetPasswordRequest(String token, String password, String confirmPassword) {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken(token);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}