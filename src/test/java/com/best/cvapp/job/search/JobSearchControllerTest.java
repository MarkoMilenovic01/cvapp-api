package com.best.cvapp.job.search;

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
class JobSearchControllerTest {

    private static final String TEST_PASSWORD = "Password123!";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String companyToken;
    private String userToken;

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
        userToken = registerUserAndGetToken("user@best.com");
    }

    @Test
    void shouldReturnAllJobsWithNoSearchFilters() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"));
    }

    @Test
    void shouldSearchJobsByKeywordSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");
        createJobAndGetId("Frontend Developer Student", "Ljubljana", "REMOTE", "STUDENT_WORK");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Backend")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"));
    }

    @Test
    void shouldSearchJobsByKeywordInRequirementsSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Spring Boot")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"));
    }

    @Test
    void shouldSearchJobsByLocationSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");
        createJobAndGetId("Frontend Developer Student", "Ljubljana", "REMOTE", "STUDENT_WORK");

        mockMvc.perform(get("/api/jobs/search")
                        .param("location", "Ljubljana")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].location").value("Ljubljana"));
    }

    @Test
    void shouldSearchJobsByWorkModeSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("workMode", "HYBRID")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].workMode").value("HYBRID"));
    }

    @Test
    void shouldSearchJobsByEmploymentTypeSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("employmentType", "INTERNSHIP")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].employmentType").value("INTERNSHIP"));
    }

    @Test
    void shouldSearchJobsByWorkModeAndEmploymentTypeSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");
        createJobAndGetId("Frontend Developer Student", "Ljubljana", "REMOTE", "STUDENT_WORK");

        mockMvc.perform(get("/api/jobs/search")
                        .param("workMode", "REMOTE")
                        .param("employmentType", "STUDENT_WORK")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Frontend Developer Student"))
                .andExpect(jsonPath("$.content[0].workMode").value("REMOTE"))
                .andExpect(jsonPath("$.content[0].employmentType").value("STUDENT_WORK"));
    }

    @Test
    void shouldSearchJobsByCompanyNameSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("companyName", "BEST")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].companyName").value("BEST Niš"));
    }

    @Test
    void shouldReturnEmptyWhenJobKeywordMatchesNothing() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "doesnotexist12345")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldReturnEmptyWhenWorkModeDoesNotMatch() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("workMode", "ONSITE")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldFindJobWithCombinedFilters() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Backend")
                        .param("location", "Maribor")
                        .param("workMode", "HYBRID")
                        .param("employmentType", "INTERNSHIP")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"));
    }

    @Test
    void shouldReturnEmptyWhenJobCombinedFiltersContradict() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Backend")
                        .param("workMode", "ONSITE")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldSearchJobsCaseInsensitively() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "backend")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldOnlyReturnActiveJobsInSearch() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        jdbcTemplate.execute("UPDATE jobs SET active = false WHERE title = 'Backend Developer Intern'");

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Backend")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldReturnPaginatedJobResults() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldRejectJobSearchWithCompanyRole() throws Exception {
        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "java")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectJobSearchWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "java"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCorrectJobFieldsInSearchResult() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor", "HYBRID", "INTERNSHIP");

        mockMvc.perform(get("/api/jobs/search")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].companyName").value("BEST Niš"))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"))
                .andExpect(jsonPath("$.content[0].location").value("Maribor"))
                .andExpect(jsonPath("$.content[0].employmentType").value("INTERNSHIP"))
                .andExpect(jsonPath("$.content[0].workMode").value("HYBRID"))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    private Long createJobAndGetId(String title, String location, String workMode, String employmentType) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody(title, location, workMode, employmentType)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String jobBody(String title, String location, String workMode, String employmentType) {
        String description = title.contains("Backend")
                ? "Work on backend APIs using Spring Boot."
                : "Work on frontend UI using React.";

        String requirements = title.contains("Backend")
                ? "Java, Spring Boot, PostgreSQL"
                : "React, TypeScript, CSS";

        return """
                {
                  "title": "%s",
                  "description": "%s",
                  "requirements": "%s",
                  "location": "%s",
                  "employmentType": "%s",
                  "workMode": "%s",
                  "deadline": "2027-12-31"
                }
                """.formatted(title, description, requirements, location, employmentType, workMode);
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