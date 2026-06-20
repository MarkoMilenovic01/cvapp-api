package com.best.cvapp;

import com.best.cvapp.auth.companyinvite.CompanyInvite;
import com.best.cvapp.auth.companyinvite.CompanyInviteRepository;
import com.best.cvapp.auth.companyinvite.dto.AcceptInviteRequest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CompanyInviteControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private CompanyInviteRepository inviteRepository;
    @MockitoBean
    private JavaMailSender javaMailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE refresh_tokens, company_invites, users RESTART IDENTITY CASCADE");
        adminToken = seedAdminAndGetToken();
    }

    // --- Send Invite ---

    @Test
    void shouldSendInviteSuccessfully() throws Exception {
        mockMvc.perform(post("/api/admin/invite-company")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailSendInviteWithoutAdminToken() throws Exception {
        mockMvc.perform(post("/api/admin/invite-company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailSendInviteWithUserToken() throws Exception {
        String userToken = registerUserAndGetToken("user@test.com", "123456");

        mockMvc.perform(post("/api/admin/invite-company")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailSendInviteTwiceToSameEmail() throws Exception {
        mockMvc.perform(post("/api/admin/invite-company")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/admin/invite-company")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com"))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailSendInviteToAlreadyRegisteredEmail() throws Exception {
        registerUserAndGetToken("existing@test.com", "123456");

        mockMvc.perform(post("/api/admin/invite-company")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("existing@test.com"))))
                .andExpect(status().isConflict());
    }

    // --- Accept Invite ---

    @Test
    void shouldAcceptInviteSuccessfully() throws Exception {
        String token = saveInvite("company@test.com", false, LocalDateTime.now().plusHours(48));

        mockMvc.perform(post("/api/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("COMPANY"));
    }

    @Test
    void shouldFailAcceptInviteWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest("invalid-token", "password123"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAcceptInviteAlreadyUsed() throws Exception {
        String token = saveInvite("company@test.com", true, LocalDateTime.now().plusHours(48));

        mockMvc.perform(post("/api/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "password123"))))
                .andExpect(status().isGone());
    }

    @Test
    void shouldFailAcceptInviteExpired() throws Exception {
        String token = saveInvite("company@test.com", false, LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/api/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "password123"))))
                .andExpect(status().isGone());
    }

    @Test
    void shouldLoginAfterAcceptingInvite() throws Exception {
        String token = saveInvite("company@test.com", false, LocalDateTime.now().plusHours(48));

        mockMvc.perform(post("/api/auth/accept-invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "password123"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("company@test.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("COMPANY"));
    }

    // --- Helpers ---

    private String seedAdminAndGetToken() throws Exception {
        jdbcTemplate.execute("""
            INSERT INTO users (email, password, role, provider, enabled)
            VALUES ('admin@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'LOCAL', true)
            """);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("admin@test.com", "password"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String registerUserAndGetToken(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest(email, password, password);


        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String saveInvite(String email, boolean used, LocalDateTime expiresAt) {
        String token = UUID.randomUUID().toString();
        CompanyInvite invite = CompanyInvite.builder()
                .email(email)
                .token(token)
                .used(used)
                .expiresAt(expiresAt)
                .build();
        inviteRepository.save(invite);
        return token;
    }

    private record InviteRequestBody(String email) {}
    private InviteRequestBody inviteRequest(String email) { return new InviteRequestBody(email); }

    private AcceptInviteRequest acceptInviteRequest(String token, String password) {
        return new AcceptInviteRequest(token, password, password);
    }

    private LoginRequest loginRequest(String email, String password) {

        return new LoginRequest(email, password);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}