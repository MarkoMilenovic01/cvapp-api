package com.best.cvapp.company.cv;

import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CompanyCVControllerTest {

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

    // ── GET /api/company/cvs ──────────────────────────────────────────────────

    @Test
    void shouldGetAllCVsSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs?page=0&size=10")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"))
                .andExpect(jsonPath("$.content[0].lastName").value("Milenovic"))
                .andExpect(jsonPath("$.content[0].summary").value("Backend developer student"))
                .andExpect(jsonPath("$.content[0].favorite").value(false));
    }

    // ── GET /api/company/cvs/{id} ─────────────────────────────────────────────

    @Test
    void shouldGetCVByIdSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.summary").value("Backend developer student"));
    }

    @Test
    void shouldReturn404WhenCVDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/company/cvs/999")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    String createCompanyAndLogin() throws Exception {
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

    String registerUserAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, "Test@1234", "Test@1234"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    Long createCV(String token, String firstName, String summary) throws Exception {
        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "%s",
                                    "lastName": "Milenovic",
                                    "phone": "123456789",
                                    "address": "Maribor",
                                    "summary": "%s",
                                    "linkedinUrl": "https://linkedin.com/in/example",
                                    "githubUrl": "https://github.com/example"
                                }
                                """.formatted(firstName, summary)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}