package com.best.cvapp;

import com.best.cvapp.auth.dto.RegisterRequest;
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
public class SearchControllerTests {

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

    // --- CV Search (COMPANY role) ---

    @Test
    void shouldReturnAllCVsWithNoFilters() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindCVByKeywordInSummary() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search?keyword=backend")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"));
    }

    @Test
    void shouldFindCVByKeywordInFirstName() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search?keyword=Marko")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindCVBySkill() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");
        addSkill(userToken, "Java", "ADVANCED");

        mockMvc.perform(get("/api/company/cvs/search?skill=Java")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindCVByLocation() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search?location=Maribor")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnEmptyWhenCVKeywordMatchesNothing() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search?keyword=doesnotexist12345")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenSkillNotFound() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");
        addSkill(userToken, "Java", "ADVANCED");

        mockMvc.perform(get("/api/company/cvs/search?skill=COBOL")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldFindCVWithCombinedFilters() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");
        addSkill(userToken, "Java", "ADVANCED");

        mockMvc.perform(get("/api/company/cvs/search?keyword=backend&skill=Java&location=Maribor")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnEmptyWhenCVCombinedFiltersContradict() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        // CV is in Maribor, not Ljubljana
        mockMvc.perform(get("/api/company/cvs/search?keyword=backend&location=Ljubljana")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldSearchCVsCaseInsensitively() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search?keyword=BACKEND")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnPaginatedCVResults() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Maribor", "Backend developer student");

        mockMvc.perform(get("/api/company/cvs/search?page=0&size=5")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldRejectCVSearchWithUserRole() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/company/cvs/search?keyword=java")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCVSearchWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/company/cvs/search?keyword=java"))
                .andExpect(status().isUnauthorized());
    }

    // --- Job Search (USER role) ---

    @Test
    void shouldReturnAllJobsWithNoFilters() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindJobByKeywordInTitle() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?keyword=Backend")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"));
    }

    @Test
    void shouldFindJobByKeywordInRequirements() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?keyword=Spring Boot")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindJobByLocation() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?location=Maribor")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindJobByWorkMode() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?workMode=HYBRID")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindJobByEmploymentType() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?employmentType=INTERNSHIP")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldFindJobByCompanyName() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?companyName=BEST Nis")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnEmptyWhenJobKeywordMatchesNothing() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?keyword=doesnotexist12345")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenWorkModeDoesNotMatch() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?workMode=ONSITE")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldFindJobWithCombinedFilters() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?keyword=Backend&location=Maribor&workMode=HYBRID&employmentType=INTERNSHIP")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnEmptyWhenJobCombinedFiltersContradict() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        // Job is HYBRID, not ONSITE
        mockMvc.perform(get("/api/jobs/search?keyword=Backend&workMode=ONSITE")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldSearchJobsCaseInsensitively() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?keyword=backend")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldOnlyReturnActiveJobsInSearch() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        jdbcTemplate.execute("UPDATE jobs SET active = false WHERE title = 'Backend Developer Intern'");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?keyword=Backend")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturnPaginatedJobResults() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search?page=0&size=5")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldRejectJobSearchWithCompanyRole() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");

        mockMvc.perform(get("/api/jobs/search?keyword=java")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectJobSearchWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/jobs/search?keyword=java"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCorrectJobFieldsInSearchResult() throws Exception {
        String companyToken = createCompanyAndLogin("company@best.com", "BEST Nis");
        createJob(companyToken, "Backend Developer Intern", "Maribor, Slovenia", "INTERNSHIP", "HYBRID");

        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/jobs/search")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].title").value("Backend Developer Intern"))
                .andExpect(jsonPath("$.content[0].companyName").value("BEST Nis"))
                .andExpect(jsonPath("$.content[0].employmentType").value("INTERNSHIP"))
                .andExpect(jsonPath("$.content[0].workMode").value("HYBRID"))
                .andExpect(jsonPath("$.content[0].location").value("Maribor, Slovenia"));
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
                        .content("""
                                { "email": "%s", "password": "password" }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String registerUserAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("123456");
        request.setConfirmPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private void createCV(String token, String firstName, String address, String summary) throws Exception {
        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "%s",
                                    "lastName": "Milenovic",
                                    "phone": "123456789",
                                    "address": "%s",
                                    "summary": "%s"
                                }
                                """.formatted(firstName, address, summary)))
                .andExpect(status().isOk());
    }

    private void addSkill(String token, String name, String level) throws Exception {
        mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "%s", "level": "%s" }
                                """.formatted(name, level)))
                .andExpect(status().isOk());
    }

    private void createJob(String token, String title, String location,
                           String employmentType, String workMode) throws Exception {
        mockMvc.perform(post("/api/company/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "%s",
                                    "description": "We are looking for a motivated intern to build REST APIs.",
                                    "requirements": "Java, Spring Boot, PostgreSQL, Git",
                                    "location": "%s",
                                    "employmentType": "%s",
                                    "workMode": "%s",
                                    "deadline": "2026-12-31"
                                }
                                """.formatted(title, location, employmentType, workMode)))
                .andExpect(status().isCreated());
    }
}