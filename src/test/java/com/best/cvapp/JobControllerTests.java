package com.best.cvapp;

import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.company.Company;
import com.best.cvapp.company.CompanyRepository;
import com.best.cvapp.user.AuthProvider;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                job_applications,
                jobs,
                favorite_cvs,
                cv_views,
                companies,
                skills,
                experience,
                education,
                cvs,
                refresh_tokens,
                users
                RESTART IDENTITY CASCADE
                """);
    }

    // --- Company Job Management ---

    @Test
    void shouldCreateJobSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");

        mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobRequestJson(
                                "Backend Developer Intern",
                                "Build REST APIs and backend services.",
                                "Java, Spring Boot, PostgreSQL",
                                "Maribor, Slovenia",
                                "INTERNSHIP",
                                "HYBRID",
                                "2026-07-31"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Backend Developer Intern"))
                .andExpect(jsonPath("$.description").value("Build REST APIs and backend services."))
                .andExpect(jsonPath("$.requirements").value("Java, Spring Boot, PostgreSQL"))
                .andExpect(jsonPath("$.location").value("Maribor, Slovenia"))
                .andExpect(jsonPath("$.employmentType").value("INTERNSHIP"))
                .andExpect(jsonPath("$.workMode").value("HYBRID"))
                .andExpect(jsonPath("$.deadline").value("2026-07-31"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.companyName").value("BEST Nis"));
    }

    @Test
    void shouldGetMyCompanyJobsSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");

        createJob(companyToken, "Backend Developer Intern");
        createJob(companyToken, "Frontend Developer Intern");

        mockMvc.perform(get("/api/company/jobs?page=0&size=10")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldGetMyCompanyJobByIdSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        mockMvc.perform(get("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.title").value("Backend Developer Intern"))
                .andExpect(jsonPath("$.companyName").value("BEST Nis"));
    }

    @Test
    void shouldUpdateJobSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        mockMvc.perform(put("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobRequestJson(
                                "Junior Backend Developer",
                                "Updated job description.",
                                "Java, Spring Boot, Docker",
                                "Ljubljana, Slovenia",
                                "STUDENT_WORK",
                                "REMOTE",
                                "2026-08-15"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Junior Backend Developer"))
                .andExpect(jsonPath("$.description").value("Updated job description."))
                .andExpect(jsonPath("$.requirements").value("Java, Spring Boot, Docker"))
                .andExpect(jsonPath("$.location").value("Ljubljana, Slovenia"))
                .andExpect(jsonPath("$.employmentType").value("STUDENT_WORK"))
                .andExpect(jsonPath("$.workMode").value("REMOTE"))
                .andExpect(jsonPath("$.deadline").value("2026-08-15"));
    }

    @Test
    void shouldDeleteJobSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        mockMvc.perform(delete("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenCompanyJobDoesNotExist() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");

        mockMvc.perform(get("/api/company/jobs/999")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowCompanyToAccessAnotherCompanyJob() throws Exception {
        String firstCompanyToken = createCompanyAndLogin("company1@best.com", "BEST Nis");
        String secondCompanyToken = createCompanyAndLogin("company2@best.com", "Google");

        Long jobId = createJob(firstCompanyToken, "Backend Developer Intern");

        mockMvc.perform(get("/api/company/jobs/" + jobId)
                        .header("Authorization", "Bearer " + secondCompanyToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectUserCreatingCompanyJob() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobRequestJson(
                                "Backend Developer Intern",
                                "Build APIs.",
                                "Java",
                                "Maribor",
                                "INTERNSHIP",
                                "HYBRID",
                                "2026-07-31"
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCreateJobWithoutToken() throws Exception {
        mockMvc.perform(post("/api/company/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobRequestJson(
                                "Backend Developer Intern",
                                "Build APIs.",
                                "Java",
                                "Maribor",
                                "INTERNSHIP",
                                "HYBRID",
                                "2026-07-31"
                        )))
                .andExpect(status().isUnauthorized());
    }

    // --- User Job Browsing ---

    @Test
    void shouldGetAllActiveJobsAsUser() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs?page=0&size=10")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"))
                .andExpect(jsonPath("$.content[0].companyName").value("BEST Nis"));
    }

    @Test
    void shouldGetActiveJobByIdAsUser() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.title").value("Backend Developer Intern"));
    }

    @Test
    void shouldRejectCompanyBrowsingUserJobsEndpoint() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    // --- User Job Applications ---

    @Test
    void shouldApplyToJobSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.jobTitle").value("Backend Developer Intern"))
                .andExpect(jsonPath("$.companyName").value("BEST Nis"))
                .andExpect(jsonPath("$.applicantFirstName").value("Marko"))
                .andExpect(jsonPath("$.applicantLastName").value("Milenovic"))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void shouldReturn409WhenUserAppliesTwice() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");

        applyToJob(userToken, jobId);

        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenApplyingWithoutCV() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetMyApplicationsSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");
        applyToJob(userToken, jobId);

        mockMvc.perform(get("/api/user/applications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobId").value(jobId))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
    }

    @Test
    void shouldWithdrawApplicationSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");

        Long applicationId = applyToJob(userToken, jobId);

        mockMvc.perform(delete("/api/user/applications/" + applicationId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user/applications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- Company Application Review ---

    @Test
    void shouldGetApplicationsForCompanyJobSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");
        applyToJob(userToken, jobId);

        mockMvc.perform(get("/api/company/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobId").value(jobId))
                .andExpect(jsonPath("$[0].applicantFirstName").value("Marko"))
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
    }

    @Test
    void shouldUpdateApplicationStatusSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");

        Long applicationId = applyToJob(userToken, jobId);

        mockMvc.perform(patch("/api/company/applications/" + applicationId + "/status")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "SHORTLISTED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(applicationId))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void shouldNotAllowCompanyToViewApplicationsForAnotherCompanyJob() throws Exception {
        String firstCompanyToken = createCompanyAndLogin("company1@best.com", "BEST Nis");
        String secondCompanyToken = createCompanyAndLogin("company2@best.com", "Google");

        Long jobId = createJob(firstCompanyToken, "Backend Developer Intern");

        mockMvc.perform(get("/api/company/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + secondCompanyToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectCompanyApplyingToJob() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        Long jobId = createJob(companyToken, "Backend Developer Intern");

        mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    // --- Helpers ---

    private String createCompanyAndLogin(String email, String companyName) throws Exception {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password"))
                .role(Role.COMPANY)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        userRepository.save(user);

        Company company = Company.builder()
                .user(user)
                .name(companyName)
                .description("Company description")
                .website("https://example.com")
                .industry("IT")
                .build();

        companyRepository.save(company);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest(email, "password"))))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result);
    }

    private String registerUserAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest(email, "123456"))))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result);
    }

    private Long createJob(String companyToken, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobRequestJson(
                                title,
                                "Build REST APIs and backend services.",
                                "Java, Spring Boot, PostgreSQL",
                                "Maribor, Slovenia",
                                "INTERNSHIP",
                                "HYBRID",
                                "2026-07-31"
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    private Long createCV(String userToken, String firstName, String summary) throws Exception {
        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson(firstName, summary)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    private Long applyToJob(String userToken, Long jobId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/jobs/" + jobId + "/apply")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private RegisterRequest registerRequest(String email, String password) {
        return new RegisterRequest(email, password, password);
    }

    private LoginRequest loginRequest(String email, String password) {

        return new LoginRequest(email, password);
    }

    private String jobRequestJson(
            String title,
            String description,
            String requirements,
            String location,
            String employmentType,
            String workMode,
            String deadline
    ) {
        return """
                {
                    "title": "%s",
                    "description": "%s",
                    "requirements": "%s",
                    "location": "%s",
                    "employmentType": "%s",
                    "workMode": "%s",
                    "deadline": "%s"
                }
                """.formatted(
                title,
                description,
                requirements,
                location,
                employmentType,
                workMode,
                deadline
        );
    }

    private String cvRequestJson(String firstName, String summary) {
        return """
                {
                    "firstName": "%s",
                    "lastName": "Milenovic",
                    "phone": "123456789",
                    "address": "Maribor",
                    "summary": "%s",
                    "linkedinUrl": "https://linkedin.com/in/example",
                    "githubUrl": "https://github.com/example"
                }
                """.formatted(firstName, summary);
    }

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}