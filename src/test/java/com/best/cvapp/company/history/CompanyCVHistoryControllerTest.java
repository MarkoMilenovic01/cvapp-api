package com.best.cvapp.company.history;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CompanyCVHistoryControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Password123!";

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

    @Test
    void shouldReturnEmptyHistorySuccessfully() throws Exception {
        mockMvc.perform(get("/api/company/history")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRecordCVViewSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/company/history")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cvId").value(cvId))
                .andExpect(jsonPath("$[0].firstName").value("Marko"))
                .andExpect(jsonPath("$[0].lastName").value("Milenovic"))
                .andExpect(jsonPath("$[0].viewedAt").exists());
    }

    @Test
    void shouldNotDuplicateHistoryWhenSameCVIsViewedTwice() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/company/history")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cvId").value(cvId))
                .andExpect(jsonPath("$[0].firstName").value("Marko"));
    }

    @Test
    void shouldReturnHistoryOrderedByNewestViewFirst() throws Exception {
        String firstUserToken = registerUserAndGetToken("user1@best.com");
        String secondUserToken = registerUserAndGetToken("user2@best.com");

        Long firstCvId = createCV(firstUserToken, "Marko", "Backend developer student");
        Long secondCvId = createCV(secondUserToken, "Ana", "Frontend developer student");

        mockMvc.perform(get("/api/company/cvs/" + firstCvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        Thread.sleep(20);

        mockMvc.perform(get("/api/company/cvs/" + secondCvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/company/history")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cvId").value(secondCvId))
                .andExpect(jsonPath("$[0].firstName").value("Ana"))
                .andExpect(jsonPath("$[1].cvId").value(firstCvId))
                .andExpect(jsonPath("$[1].firstName").value("Marko"));
    }

    @Test
    void shouldReturn403WhenUserTriesToAccessCompanyHistory() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/company/history")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private String createCompanyAndLogin() throws Exception {
        User companyUser = new User();
        companyUser.setEmail("company@best.com");
        companyUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        companyUser.setRole(Role.COMPANY);
        companyUser.setProvider(AuthProvider.LOCAL);
        companyUser.setEnabled(true);

        userRepository.save(companyUser);

        Company company = new Company();
        company.setUser(companyUser);
        company.setName("BEST Niš");
        company.setDescription("Student organization");
        company.setWebsite("https://best.rs");
        company.setIndustry("Education");

        companyRepository.save(company);

        return loginAndGetToken("company@best.com", TEST_PASSWORD);
    }

    private String registerUserAndGetToken(String email) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        userRepository.save(user);

        return loginAndGetToken(email, TEST_PASSWORD);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result);
    }

    private Long createCV(String token, String firstName, String summary) throws Exception {
        String body = """
                {
                  "firstName": "%s",
                  "lastName": "Milenovic",
                  "summary": "%s",
                  "location": "Maribor",
                  "skills": [],
                  "education": [],
                  "experience": []
                }
                """.formatted(firstName, summary);

        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        return json.get("id").asLong();
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
