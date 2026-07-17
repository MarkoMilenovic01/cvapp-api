package com.best.cvapp.cv.project;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.emailverification.dto.VerifyEmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Test@1234";
    private static final String PROJECTS_URL = "/api/user/cv/projects";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        truncateDatabase();

        userToken = registerVerifyLoginAndGetAccessToken("user@best.com");
        createCV(userToken);
    }

    @Test
    void shouldGetEmptyProjectListSuccessfully() throws Exception {
        mockMvc.perform(get(PROJECTS_URL)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldAddProjectSuccessfully() throws Exception {
        mockMvc.perform(post(PROJECTS_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                "CV App",
                                "A CV building and recruitment platform",
                                "https://cvapp.example.com",
                                "https://github.com/marko/cvapp",
                                true
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("CV App"))
                .andExpect(jsonPath("$.description").value("A CV building and recruitment platform"))
                .andExpect(jsonPath("$.projectUrl").value("https://cvapp.example.com"))
                .andExpect(jsonPath("$.repositoryUrl").value("https://github.com/marko/cvapp"))
                .andExpect(jsonPath("$.startDate").value("2025-01-01"))
                .andExpect(jsonPath("$.endDate").doesNotExist())
                .andExpect(jsonPath("$.current").value(true));
    }

    @Test
    void shouldUpdateProjectSuccessfully() throws Exception {
        Long projectId = addProjectAndGetId();

        mockMvc.perform(put(PROJECTS_URL + "/" + projectId)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                "Updated CV App",
                                "An updated project description",
                                "https://updated-cvapp.example.com",
                                "https://github.com/marko/updated-cvapp",
                                false
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Updated CV App"))
                .andExpect(jsonPath("$.description").value("An updated project description"))
                .andExpect(jsonPath("$.projectUrl").value("https://updated-cvapp.example.com"))
                .andExpect(jsonPath("$.repositoryUrl").value("https://github.com/marko/updated-cvapp"))
                .andExpect(jsonPath("$.startDate").value("2025-01-01"))
                .andExpect(jsonPath("$.current").value(false));
    }

    @Test
    void shouldDeleteProjectSuccessfully() throws Exception {
        Long projectId = addProjectAndGetId();

        mockMvc.perform(delete(PROJECTS_URL + "/" + projectId)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(PROJECTS_URL)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetAllProjectsSuccessfully() throws Exception {
        addProjectAndGetId();

        mockMvc.perform(post(PROJECTS_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                "Task Manager",
                                "A task management application",
                                "https://tasks.example.com",
                                "https://github.com/marko/task-manager",
                                false
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(get(PROJECTS_URL)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn404WhenProjectNotFound() throws Exception {
        mockMvc.perform(delete(PROJECTS_URL + "/999")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddProjectWithoutCV() throws Exception {
        truncateDatabase();

        String tokenWithoutCV = registerVerifyLoginAndGetAccessToken("user2@best.com");

        mockMvc.perform(post(PROJECTS_URL)
                        .header("Authorization", bearer(tokenWithoutCV))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                "CV App",
                                "A CV building and recruitment platform",
                                "https://cvapp.example.com",
                                "https://github.com/marko/cvapp",
                                true
                        )))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddProjectWithoutToken() throws Exception {
        mockMvc.perform(post(PROJECTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                "CV App",
                                "A CV building and recruitment platform",
                                "https://cvapp.example.com",
                                "https://github.com/marko/cvapp",
                                true
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailAddProjectWithInvalidBody() throws Exception {
        String body = """
                {
                  "name": "",
                  "description": "A project without a name",
                  "projectUrl": "https://cvapp.example.com",
                  "repositoryUrl": "https://github.com/marko/cvapp",
                  "startDate": "2025-01-01",
                  "endDate": null,
                  "current": true
                }
                """;

        mockMvc.perform(post(PROJECTS_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private Long addProjectAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post(PROJECTS_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody(
                                "CV App",
                                "A CV building and recruitment platform",
                                "https://cvapp.example.com",
                                "https://github.com/marko/cvapp",
                                true
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String projectBody(
            String name,
            String description,
            String projectUrl,
            String repositoryUrl,
            boolean current
    ) {
        return """
                {
                  "name": "%s",
                  "description": "%s",
                  "projectUrl": "%s",
                  "repositoryUrl": "%s",
                  "startDate": "2025-01-01",
                  "endDate": null,
                  "current": %s
                }
                """.formatted(name, description, projectUrl, repositoryUrl, current);
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
                  "githubUrl": "https://github.com/marko"
                }
                """;

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private void truncateDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                email_verification_tokens,
                favorite_cvs, cv_views, companies,
                projects, skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String registerVerifyLoginAndGetAccessToken(String email) throws Exception {
        register(email);
        verifyEmail(email);
        return extractAccessToken(login(email));
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RegisterRequest(email, TEST_PASSWORD, TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    private void verifyEmail(String email) throws Exception {
        String token = fetchVerificationToken(email);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VerifyEmailRequest(token))))
                .andExpect(status().isNoContent());
    }

    private MvcResult login(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(email, TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();
    }

    private String fetchVerificationToken(String email) {
        return storeKnownVerificationToken(jdbcTemplate, email);
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
