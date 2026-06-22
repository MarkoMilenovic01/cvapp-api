package com.best.cvapp.cv.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CVControllerTest {

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
    }

    @Test
    void shouldCreateCVSuccessfully() throws Exception {
        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvBody("Marko", "Milenovic", "Backend developer student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.phone").value("+38612345678"))
                .andExpect(jsonPath("$.address").value("Maribor"))
                .andExpect(jsonPath("$.summary").value("Backend developer student"))
                .andExpect(jsonPath("$.linkedinUrl").value("https://linkedin.com/in/marko"))
                .andExpect(jsonPath("$.githubUrl").value("https://github.com/marko"))
                .andExpect(jsonPath("$.skills.length()").value(0))
                .andExpect(jsonPath("$.education.length()").value(0))
                .andExpect(jsonPath("$.experience.length()").value(0));
    }

    @Test
    void shouldGetMyCVSuccessfully() throws Exception {
        Long cvId = createCV(userToken, "Marko", "Milenovic", "Backend developer student");

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.summary").value("Backend developer student"));
    }

    @Test
    void shouldUpdateCVSuccessfully() throws Exception {
        Long cvId = createCV(userToken, "Marko", "Milenovic", "Backend developer student");

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvBody("Marko", "Milenovic", "AI engineer student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.summary").value("AI engineer student"));

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.summary").value("AI engineer student"));
    }

    @Test
    void shouldDeleteCVSuccessfully() throws Exception {
        createCV(userToken, "Marko", "Milenovic", "Backend developer student");

        mockMvc.perform(delete("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenCVNotFound() throws Exception {
        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectGetCVWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/cv"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentCV() throws Exception {
        mockMvc.perform(delete("/api/user/cv")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    private Long createCV(String token, String firstName, String lastName, String summary) throws Exception {
        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvBody(firstName, lastName, summary)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String cvBody(String firstName, String lastName, String summary) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "phone": "+38612345678",
                  "address": "Maribor",
                  "summary": "%s",
                  "linkedinUrl": "https://linkedin.com/in/marko",
                  "githubUrl": "https://github.com/marko",
                  "skills": [],
                  "education": [],
                  "experience": []
                }
                """.formatted(firstName, lastName, summary);
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