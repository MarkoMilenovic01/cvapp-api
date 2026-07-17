package com.best.cvapp.auth.companyinvite;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.companyinvite.dto.AcceptInviteRequest;
import com.best.cvapp.auth.companyinvite.dto.InviteRequest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.emailverification.dto.VerifyEmailRequest;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CompanyInviteControllerTest extends AbstractIntegrationTest {

    private static final String AUTH_URL = "/api/auth";
    private static final String INVITES_URL = "/api/auth/company-invites";
    private static final String COMPANY_EMAIL = "company@test.com";
    private static final String COMPANY_NAME = "Test Company";
    private static final String TEST_PASSWORD = "Test@1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CompanyInviteRepository inviteRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("""
            TRUNCATE TABLE company_invites, email_verification_tokens, refresh_tokens, users
            RESTART IDENTITY CASCADE
            """);

        adminToken = seedAdminAndGetToken();
    }

    // ── Send Invite ───────────────────────────────────────────────────────────

    @Test
    void shouldSendInviteSuccessfully() throws Exception {
        sendInvite(COMPANY_EMAIL, COMPANY_NAME, adminToken)
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailSendInviteWithoutToken() throws Exception {
        mockMvc.perform(post(INVITES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new InviteRequest(COMPANY_EMAIL, COMPANY_NAME))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailSendInviteWithUserToken() throws Exception {
        String userToken = registerVerifyAndLoginGetToken("user@test.com", TEST_PASSWORD);

        sendInvite(COMPANY_EMAIL, COMPANY_NAME, userToken)
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailSendInviteTwiceToSameEmail() throws Exception {
        sendInvite(COMPANY_EMAIL, COMPANY_NAME, adminToken)
                .andExpect(status().isNoContent());

        sendInvite(COMPANY_EMAIL, COMPANY_NAME, adminToken)
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailSendInviteToAlreadyRegisteredEmail() throws Exception {
        registerAndVerify("existing@test.com", TEST_PASSWORD);

        sendInvite("existing@test.com", COMPANY_NAME, adminToken)
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAllowReInviteAfterExpiry() throws Exception {
        saveInvite(COMPANY_EMAIL, false, LocalDateTime.now().minusHours(1));

        sendInvite(COMPANY_EMAIL, COMPANY_NAME, adminToken)
                .andExpect(status().isNoContent());
    }

    // ── Accept Invite ─────────────────────────────────────────────────────────

    @Test
    void shouldAcceptInviteSuccessfully() throws Exception {
        String token = saveInvite(
                COMPANY_EMAIL,
                false,
                LocalDateTime.now().plusHours(48)
        );

        acceptInvite(token, TEST_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("COMPANY"));
    }

    @Test
    void shouldFailAcceptInviteWithInvalidToken() throws Exception {
        acceptInvite("invalid-token", TEST_PASSWORD)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailAcceptInviteAlreadyUsed() throws Exception {
        String token = saveInvite(
                COMPANY_EMAIL,
                true,
                LocalDateTime.now().plusHours(48)
        );

        acceptInvite(token, TEST_PASSWORD)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailAcceptExpiredInvite() throws Exception {
        String token = saveInvite(
                COMPANY_EMAIL,
                false,
                LocalDateTime.now().minusHours(1)
        );

        acceptInvite(token, TEST_PASSWORD)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginAfterAcceptingInvite() throws Exception {
        String token = saveInvite(
                COMPANY_EMAIL,
                false,
                LocalDateTime.now().plusHours(48)
        );

        acceptInvite(token, TEST_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("COMPANY"));

        mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(COMPANY_EMAIL, TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("COMPANY"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String seedAdminAndGetToken() throws Exception {
        registerAndVerify("admin@test.com", "Password123!");

        User admin = userRepository.findByEmail("admin@test.com")
                .orElseThrow();

        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        MvcResult result = login("admin@test.com", "Password123!");

        return extractAccessToken(result);
    }

    private String registerVerifyAndLoginGetToken(String email, String password) throws Exception {
        registerAndVerify(email, password);

        MvcResult result = login(email, password);

        return extractAccessToken(result);
    }

    private MvcResult register(String email, String password) throws Exception {
        return mockMvc.perform(post(AUTH_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void registerAndVerify(String email, String password) throws Exception {
        register(email, password);
        verifyEmail(email);
    }

    private void verifyEmail(String email) throws Exception {
        String token = fetchVerificationToken(email);

        mockMvc.perform(post(AUTH_URL + "/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest(token))))
                .andExpect(status().isNoContent());
    }

    private String fetchVerificationToken(String email) {
        return storeKnownVerificationToken(jdbcTemplate, email);
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();

        return objectMapper.readTree(body)
                .get("accessToken")
                .asText();
    }

    private String saveInvite(String email, boolean used, LocalDateTime expiresAt) {
        String token = UUID.randomUUID().toString();

        inviteRepository.save(CompanyInvite.builder()
                .email(email)
                .companyName("Test Company")
                .token(DigestUtils.sha256Hex(token))
                .used(used)
                .expiresAt(expiresAt)
                .build());

        return token;
    }

    private ResultActions sendInvite(String email, String companyName, String accessToken) throws Exception {
        return mockMvc.perform(post(INVITES_URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new InviteRequest(email, companyName))));
    }

    private ResultActions acceptInvite(String token, String password) throws Exception {
        return mockMvc.perform(post(INVITES_URL + "/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new AcceptInviteRequest(token, password, password))));
    }

    private RegisterRequest registerRequest(String email, String password) {
        return new RegisterRequest(email, password, password);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
