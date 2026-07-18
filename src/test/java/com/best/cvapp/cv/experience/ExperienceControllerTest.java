package com.best.cvapp.cv.experience;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExperienceControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Test@1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                email_verification_tokens,
                favorite_cvs, cv_views, companies,
                skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        userToken = registerVerifyLoginAndGetAccessToken("user@best.com");
        createCV(userToken);
    }

    @Test
    void shouldGetEmptyExperienceListSuccessfully() throws Exception {
        mockMvc.perform(get("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldAddExperienceSuccessfully() throws Exception {
        mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody(
                                "BEST Niš",
                                "Backend Developer",
                                ExperienceType.INTERNSHIP,
                                "Built REST APIs",
                                true
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.companyName").value("BEST Niš"))
                .andExpect(jsonPath("$.position").value("Backend Developer"))
                .andExpect(jsonPath("$.experienceType").value("INTERNSHIP"))
                .andExpect(jsonPath("$.description").value("Built REST APIs"))
                .andExpect(jsonPath("$.startDate").value("2025-01-01"))
                .andExpect(jsonPath("$.current").value(true));
    }

    @Test
    void shouldUpdateExperienceSuccessfully() throws Exception {
        Long experienceId = addExperienceAndGetId();

        mockMvc.perform(put("/api/user/cv/experience/" + experienceId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody(
                                "FERI",
                                "Software Engineer",
                                ExperienceType.FULL_TIME,
                                "Worked on backend systems",
                                false
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(experienceId))
                .andExpect(jsonPath("$.companyName").value("FERI"))
                .andExpect(jsonPath("$.position").value("Software Engineer"))
                .andExpect(jsonPath("$.experienceType").value("FULL_TIME"))
                .andExpect(jsonPath("$.description").value("Worked on backend systems"))
                .andExpect(jsonPath("$.current").value(false));
    }

    @Test
    void shouldDeleteExperienceSuccessfully() throws Exception {
        Long experienceId = addExperienceAndGetId();

        mockMvc.perform(delete("/api/user/cv/experience/" + experienceId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetAllExperienceSuccessfully() throws Exception {
        addExperienceAndGetId();

        mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody(
                                "Google",
                                "Software Engineer",
                                ExperienceType.FULL_TIME,
                                "Worked on large systems",
                                false
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn404WhenExperienceNotFound() throws Exception {
        mockMvc.perform(delete("/api/user/cv/experience/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddExperienceWithoutCV() throws Exception {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                email_verification_tokens,
                favorite_cvs, cv_views, companies,
                skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        String tokenWithoutCV = registerVerifyLoginAndGetAccessToken("user2@best.com");

        mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + tokenWithoutCV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody(
                                "BEST Niš",
                                "Backend Developer",
                                ExperienceType.INTERNSHIP,
                                "Built REST APIs",
                                true
                        )))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddExperienceWithoutToken() throws Exception {
        mockMvc.perform(post("/api/user/cv/experience")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody(
                                "BEST Niš",
                                "Backend Developer",
                                ExperienceType.INTERNSHIP,
                                "Built REST APIs",
                                true
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailAddExperienceWithInvalidBody() throws Exception {
        String body = """
                {
                  "companyName": "",
                  "position": "",
                  "experienceType": null,
                  "description": "Built REST APIs",
                  "startDate": "2025-01-01",
                  "endDate": null,
                  "current": true
                }
                """;

        mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private Long addExperienceAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody(
                                "BEST Niš",
                                "Backend Developer",
                                ExperienceType.INTERNSHIP,
                                "Built REST APIs",
                                true
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String experienceBody(
            String companyName,
            String position,
            ExperienceType experienceType,
            String description,
            boolean current
    ) {
        return """
                {
                  "companyName": "%s",
                  "position": "%s",
                  "experienceType": "%s",
                  "description": "%s",
                  "startDate": "2025-01-01",
                  "endDate": null,
                  "current": %s
                }
                """.formatted(companyName, position, experienceType.name(), description, current);
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
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    // ── Auth helpers ──────────────────────────────────────────────────────────

    private String registerVerifyLoginAndGetAccessToken(String email) throws Exception {
        register(email);
        verifyEmail(email);

        MvcResult loginResult = login(email);

        return extractAccessToken(loginResult);
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
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
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
