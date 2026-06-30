package com.best.cvapp.auth.companyinvite;

import com.best.cvapp.auth.companyinvite.dto.AcceptInviteRequest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CompanyInviteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private CompanyInviteRepository inviteRepository;
    @Autowired private UserRepository userRepository;
    @MockitoBean private JavaMailSender javaMailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE company_invites, refresh_tokens, users RESTART IDENTITY CASCADE");
        adminToken = seedAdminAndGetToken();
    }

    // ── Send Invite ───────────────────────────────────────────────────────────

    @Test
    void shouldSendInviteSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/company-invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com", "Test Company"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailSendInviteWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/company-invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com", "Test Company"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailSendInviteWithUserToken() throws Exception {
        String userToken = registerUserAndGetToken("user@test.com", "Test@1234");

        mockMvc.perform(post("/api/auth/company-invites")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com", "Test Company"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailSendInviteTwiceToSameEmail() throws Exception {
        mockMvc.perform(post("/api/auth/company-invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com", "Test Company"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/company-invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com", "Test Company"))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailSendInviteToAlreadyRegisteredEmail() throws Exception {
        registerUserAndGetToken("existing@test.com", "Test@1234");

        mockMvc.perform(post("/api/auth/company-invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("existing@test.com", "Test Company"))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAllowReInviteAfterExpiry() throws Exception {
        saveInvite("company@test.com", false, LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/api/auth/company-invites")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(inviteRequest("company@test.com", "Test Company"))))
                .andExpect(status().isNoContent());
    }

    // ── Accept Invite ─────────────────────────────────────────────────────────

    @Test
    void shouldAcceptInviteSuccessfully() throws Exception {
        String token = saveInvite("company@test.com", false, LocalDateTime.now().plusHours(48));

        mockMvc.perform(post("/api/auth/company-invites/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "Test@1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("COMPANY"));
    }

    @Test
    void shouldFailAcceptInviteWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/company-invites/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest("invalid-token", "Test@1234"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailAcceptInviteAlreadyUsed() throws Exception {
        String token = saveInvite("company@test.com", true, LocalDateTime.now().plusHours(48));

        mockMvc.perform(post("/api/auth/company-invites/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "Test@1234"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailAcceptExpiredInvite() throws Exception {
        String token = saveInvite("company@test.com", false, LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/api/auth/company-invites/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "Test@1234"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginAfterAcceptingInvite() throws Exception {
        String token = saveInvite("company@test.com", false, LocalDateTime.now().plusHours(48));

        mockMvc.perform(post("/api/auth/company-invites/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(acceptInviteRequest(token, "Test@1234"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("company@test.com", "Test@1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("COMPANY"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String seedAdminAndGetToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "admin@test.com",
                              "password": "Password123!",
                              "confirmPassword": "Password123!"
                            }
                            """))
                .andExpect(status().isOk());

        User admin = userRepository.findByEmail("admin@test.com")
                .orElseThrow();

        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "admin@test.com",
                              "password": "Password123!"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("accessToken")
                .asText();
    }

    private String registerUserAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RegisterRequest(email, password, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String saveInvite(String email, boolean used, LocalDateTime expiresAt) {
        String token = UUID.randomUUID().toString();
        inviteRepository.save(CompanyInvite.builder()
                .email(email)
                .companyName("Test Company")
                .token(token)
                .used(used)
                .expiresAt(expiresAt)
                .build());
        return token;
    }

    private record InviteRequestBody(String email, String companyName) {}

    private InviteRequestBody inviteRequest(String email, String companyName) {
        return new InviteRequestBody(email, companyName);
    }

    private AcceptInviteRequest acceptInviteRequest(String token, String password) {
        return new AcceptInviteRequest(token, password, password);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}