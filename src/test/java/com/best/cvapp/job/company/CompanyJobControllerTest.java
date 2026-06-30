package com.best.cvapp.job.company;

import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.job.core.ApplicationStatus;
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
class CompanyJobControllerTest {

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
    void shouldCreateJobSuccessfully() throws Exception {
        mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody("Backend Developer Intern", "Maribor")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Backend Developer Intern"))
                .andExpect(jsonPath("$.location").value("Maribor"))
                .andExpect(jsonPath("$.employmentType").value("INTERNSHIP"))
                .andExpect(jsonPath("$.workMode").value("HYBRID"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetMyJobsSuccessfully() throws Exception {
        createJobAndGetId("Backend Developer Intern", "Maribor");

        mockMvc.perform(get("/api/company/jobs")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"));
    }

    @Test
    void shouldGetMyJobByIdSuccessfully() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        mockMvc.perform(get("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.title").value("Backend Developer Intern"));
    }

    @Test
    void shouldUpdateJobSuccessfully() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        mockMvc.perform(put("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody("Junior Backend Developer", "Ljubljana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.title").value("Junior Backend Developer"))
                .andExpect(jsonPath("$.location").value("Ljubljana"));
    }

    @Test
    void shouldDeleteJobSuccessfully() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        mockMvc.perform(delete("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetApplicationsForJobSuccessfully() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken);

        mockMvc.perform(post("/api/user/applications/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/company/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobId").value(jobId))
                .andExpect(jsonPath("$[0].jobTitle").value("Backend Developer Intern"))
                .andExpect(jsonPath("$[0].cvFirstName").value("Marko"))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
    }

    @Test
    void shouldUpdateApplicationStatusSuccessfully() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken);

        MvcResult applyResult = mockMvc.perform(post("/api/user/applications/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andReturn();

        Long applicationId = objectMapper
                .readTree(applyResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        String body = """
                {
                  "status": "REVIEWED"
                }
                """;

        mockMvc.perform(patch("/api/company/jobs/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicationId))
                .andExpect(jsonPath("$.status").value(ApplicationStatus.REVIEWED.name()));
    }

    @Test
    void shouldReturn404WhenCompanyJobDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/company/jobs/999")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectUserCreatingCompanyJob() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody("Backend Developer Intern", "Maribor")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCreateJobWithoutToken() throws Exception {
        mockMvc.perform(post("/api/company/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody("Backend Developer Intern", "Maribor")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn404AfterDeletingJob() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        mockMvc.perform(delete("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowCompanyToAccessAnotherCompanyJob() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        String secondCompanyToken = createCompanyAndLogin("company2@best.com", "Google");

        mockMvc.perform(get("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + secondCompanyToken))
                .andExpect(status().isNotFound());
    }

    private Long createJobAndGetId(String title, String location) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody(title, location)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    @Test
    void shouldNotAllowCompanyToViewApplicationsForAnotherCompanyJob() throws Exception {
        Long jobId = createJobAndGetId("Backend Developer Intern", "Maribor");

        String secondCompanyToken = createCompanyAndLogin("company2@best.com", "Google");

        mockMvc.perform(get("/api/company/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + secondCompanyToken))
                .andExpect(status().isNotFound());
    }

    private String jobBody(String title, String location) {
        return """
                {
                  "title": "%s",
                  "description": "Work on backend APIs using Spring Boot.",
                  "requirements": "Java, Spring Boot, PostgreSQL",
                  "location": "%s",
                  "employmentType": "INTERNSHIP",
                  "workMode": "HYBRID",
                  "deadline": "2027-12-31"
                }
                """.formatted(title, location);
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

    private void createCV(String token) throws Exception {
        String body = """
                {
                  "firstName": "Marko",
                  "lastName": "Milenovic",
                  "phone": "+38612345678",
                  "address": "Maribor",
                  "summary": "Backend developer student",
                  "linkedinUrl": "https://linkedin.com/in/marko",
                  "githubUrl": "https://github.com/marko",
                  "skills": [],
                  "education": [],
                  "experience": []
                }
                """;

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private String createCompanyAndLogin(String email, String companyName) throws Exception {
        User companyUser = new User();
        companyUser.setEmail(email);
        companyUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        companyUser.setRole(Role.COMPANY);
        companyUser.setProvider(AuthProvider.LOCAL);

        userRepository.save(companyUser);

        Company company = new Company();
        company.setUser(companyUser);
        company.setName(companyName);
        company.setDescription("Company description");
        company.setWebsite("https://example.com");
        company.setIndustry("IT");

        companyRepository.save(company);

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

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}