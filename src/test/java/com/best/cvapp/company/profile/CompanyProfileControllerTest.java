package com.best.cvapp.company.profile;

import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.oauth.AuthProvider;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CompanyProfileControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String companyToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                favorite_cvs, cv_views, companies,
                skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);
        companyToken = createCompanyAndLogin();
    }

    // ── GET /api/company/me ───────────────────────────────────────────────────

    @Test
    void shouldGetCompanyProfileSuccessfully() throws Exception {
        mockMvc.perform(get("/api/company/me")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BEST Nis"))
                .andExpect(jsonPath("$.description").value("Board of European Students of Technology"))
                .andExpect(jsonPath("$.website").value("https://best.eu.org"))
                .andExpect(jsonPath("$.industry").value("Education"));
    }

    @Test
    void shouldRejectCompanyEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/company/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUserTokenOnCompanyEndpoint() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/company/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/company/me ───────────────────────────────────────────────────

    @Test
    void shouldUpdateCompanyProfileSuccessfully() throws Exception {
        mockMvc.perform(put("/api/company/me")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "BEST Maribor",
                                    "description": "Student organization focused on technology",
                                    "website": "https://best-maribor.si",
                                    "industry": "Education and Technology"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BEST Maribor"))
                .andExpect(jsonPath("$.description").value("Student organization focused on technology"))
                .andExpect(jsonPath("$.website").value("https://best-maribor.si"))
                .andExpect(jsonPath("$.industry").value("Education and Technology"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String createCompanyAndLogin() throws Exception {
        User user = User.builder()
                .email("company@best.com")
                .password(passwordEncoder.encode("Test@1234"))
                .provider(AuthProvider.LOCAL)
                .role(Role.COMPANY)
                .enabled(true)
                .build();
        userRepository.save(user);

        companyRepository.save(Company.builder()
                .user(user)
                .name("BEST Nis")
                .description("Board of European Students of Technology")
                .website("https://best.eu.org")
                .industry("Education")
                .build());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("company@best.com", "Test@1234"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String registerUserAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.best.cvapp.auth.credentials.dto.RegisterRequest(
                                        email, "Test@1234", "Test@1234"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }
}