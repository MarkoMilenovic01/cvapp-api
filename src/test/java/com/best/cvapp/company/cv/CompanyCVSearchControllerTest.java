package com.best.cvapp.company.cv;

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
class CompanyCVSearchControllerTest {

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
                job_applications, jobs,
                favorite_cvs, cv_views, companies,
                skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        companyToken = createCompanyAndLogin();
    }

    @Test
    void shouldReturnAllCVsWithNoFilters() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"));
    }

    @Test
    void shouldFindCVByKeywordInSummary() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "backend")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"));
    }

    @Test
    void shouldFindCVByKeywordInFirstName() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "Marko")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"));
    }

    @Test
    void shouldFindCVBySkill() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");
        addSkill(userToken, "Java", "Advanced");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("skill", "Java")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"));
    }

    @Test
    void shouldFindCVByLocation() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("location", "Maribor")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnEmptyWhenCVKeywordMatchesNothing() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "doesnotexist12345")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldReturnEmptyWhenSkillNotFound() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");
        addSkill(userToken, "Java", "Advanced");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("skill", "COBOL")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldFindCVWithCombinedFilters() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");
        addSkill(userToken, "Java", "Advanced");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "backend")
                        .param("skill", "Java")
                        .param("location", "Maribor")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"));
    }

    @Test
    void shouldReturnEmptyWhenCVCombinedFiltersContradict() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");
        addSkill(userToken, "Java", "Advanced");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "backend")
                        .param("skill", "Java")
                        .param("location", "Ljubljana")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldSearchCVsCaseInsensitively() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "BACKEND")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnPaginatedCVResults() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldRejectCVSearchWithUserRole() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "java")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCVSearchWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/company/cvs/search")
                        .param("keyword", "java"))
                .andExpect(status().isUnauthorized());
    }

    private String createCompanyAndLogin() throws Exception {
        User companyUser = new User();
        companyUser.setEmail("company@best.com");
        companyUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        companyUser.setRole(Role.COMPANY);
        companyUser.setProvider(AuthProvider.LOCAL);

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
        String body = """
                {
                  "email": "%s",
                  "password": "%s",
                  "confirmPassword": "%s"
                }
                """.formatted(email, TEST_PASSWORD, TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result);
    }

    private Long createCV(String token, String firstName, String address, String summary) throws Exception {
        String body = """
                {
                  "firstName": "%s",
                  "lastName": "Milenovic",
                  "phone": "+38612345678",
                  "address": "%s",
                  "summary": "%s",
                  "linkedinUrl": "https://linkedin.com/in/marko",
                  "githubUrl": "https://github.com/marko",
                  "skills": [],
                  "education": [],
                  "experience": []
                }
                """.formatted(firstName, address, summary);

        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void addSkill(String token, String name, String level) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "level": "%s"
                }
                """.formatted(name, level);

        mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
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

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}