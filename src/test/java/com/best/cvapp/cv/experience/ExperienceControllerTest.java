package com.best.cvapp.cv.experience;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExperienceControllerTest {

    private static final String TEST_PASSWORD = "Password123!";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                favorite_cvs, cv_views, companies,
                skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        userToken = registerUserAndGetToken("user@best.com");
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
                        .content(experienceBody("BEST Niš", "Backend Developer", "Built REST APIs", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.companyName").value("BEST Niš"))
                .andExpect(jsonPath("$.position").value("Backend Developer"))
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
                        .content(experienceBody("FERI", "Software Engineer", "Worked on backend systems", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(experienceId))
                .andExpect(jsonPath("$.companyName").value("FERI"))
                .andExpect(jsonPath("$.position").value("Software Engineer"))
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
                        .content(experienceBody("Google", "Software Engineer", "Worked on large systems", false)))
                .andExpect(status().isOk());

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
            favorite_cvs, cv_views, companies,
            skills, experience, education, cvs,
            refresh_tokens, users
            RESTART IDENTITY CASCADE
            """);

        userToken = registerUserAndGetToken("user2@best.com");

        mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody("BEST Niš", "Backend Developer", "Built REST APIs", true)))
                .andExpect(status().isNotFound());
    }

    private Long addExperienceAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceBody("BEST Niš", "Backend Developer", "Built REST APIs", true)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String experienceBody(String companyName, String position, String description, boolean current) {
        return """
                {
                  "companyName": "%s",
                  "position": "%s",
                  "description": "%s",
                  "startDate": "2025-01-01",
                  "endDate": null,
                  "current": %s
                }
                """.formatted(companyName, position, description, current);
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

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}