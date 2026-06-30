package com.best.cvapp.cv.education;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EducationControllerTest {

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
    void shouldGetEmptyEducationListSuccessfully() throws Exception {
        mockMvc.perform(get("/api/user/cv/education")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldAddEducationSuccessfully() throws Exception {
        mockMvc.perform(post("/api/user/cv/education")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody("University of Maribor", "Master", "AI Engineering", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.institution").value("University of Maribor"))
                .andExpect(jsonPath("$.degree").value("Master"))
                .andExpect(jsonPath("$.fieldOfStudy").value("AI Engineering"))
                .andExpect(jsonPath("$.startDate").value("2024-10-01"))
                .andExpect(jsonPath("$.current").value(true));
    }

    @Test
    void shouldUpdateEducationSuccessfully() throws Exception {
        Long educationId = addEducationAndGetId();

        mockMvc.perform(put("/api/user/cv/education/" + educationId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody("FERI", "MSc", "Artificial Intelligence", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(educationId))
                .andExpect(jsonPath("$.institution").value("FERI"))
                .andExpect(jsonPath("$.degree").value("MSc"))
                .andExpect(jsonPath("$.fieldOfStudy").value("Artificial Intelligence"))
                .andExpect(jsonPath("$.current").value(false));
    }

    @Test
    void shouldDeleteEducationSuccessfully() throws Exception {
        Long educationId = addEducationAndGetId();

        mockMvc.perform(delete("/api/user/cv/education/" + educationId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user/cv/education")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Long addEducationAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/cv/education")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody("University of Maribor", "Master", "AI Engineering", true)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    @Test
    void shouldGetAllEducationSuccessfully() throws Exception {
        addEducationAndGetId();

        mockMvc.perform(post("/api/user/cv/education")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody("University of Nis", "Bachelor", "Computer Science", false)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/user/cv/education")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn404WhenEducationNotFound() throws Exception {
        mockMvc.perform(delete("/api/user/cv/education/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddEducationWithoutCV() throws Exception {
        jdbcTemplate.execute("""
            TRUNCATE TABLE
            favorite_cvs, cv_views, companies,
            skills, experience, education, cvs,
            refresh_tokens, users
            RESTART IDENTITY CASCADE
            """);

        userToken = registerUserAndGetToken("user2@best.com");

        mockMvc.perform(post("/api/user/cv/education")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody("University of Maribor", "Master", "AI Engineering", true)))
                .andExpect(status().isNotFound());
    }

    private String educationBody(String institution, String degree, String fieldOfStudy, boolean current) {
        return """
                {
                  "institution": "%s",
                  "degree": "%s",
                  "fieldOfStudy": "%s",
                  "startDate": "2024-10-01",
                  "endDate": null,
                  "current": %s
                }
                """.formatted(institution, degree, fieldOfStudy, current);
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