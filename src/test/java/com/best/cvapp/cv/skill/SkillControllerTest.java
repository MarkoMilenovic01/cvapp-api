package com.best.cvapp.cv.skill;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SkillControllerTest {

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
    void shouldGetEmptySkillsListSuccessfully() throws Exception {
        mockMvc.perform(get("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldAddSkillSuccessfully() throws Exception {
        mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody("Java", "Advanced")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.level").value("Advanced"));
    }

    @Test
    void shouldUpdateSkillSuccessfully() throws Exception {
        Long skillId = addSkillAndGetId();

        mockMvc.perform(put("/api/user/cv/skills/" + skillId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody("Spring Boot", "Advanced")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(skillId))
                .andExpect(jsonPath("$.name").value("Spring Boot"))
                .andExpect(jsonPath("$.level").value("Advanced"));
    }

    @Test
    void shouldDeleteSkillSuccessfully() throws Exception {
        Long skillId = addSkillAndGetId();

        mockMvc.perform(delete("/api/user/cv/skills/" + skillId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetAllSkillsSuccessfully() throws Exception {
        addSkillAndGetId();

        mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody("Spring Boot", "Intermediate")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn404WhenSkillNotFound() throws Exception {
        mockMvc.perform(delete("/api/user/cv/skills/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddSkillWithoutCV() throws Exception {
        jdbcTemplate.execute("""
            TRUNCATE TABLE
            favorite_cvs, cv_views, companies,
            skills, experience, education, cvs,
            refresh_tokens, users
            RESTART IDENTITY CASCADE
            """);

        userToken = registerUserAndGetToken("user2@best.com");

        mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody("Java", "Advanced")))
                .andExpect(status().isNotFound());
    }

    private Long addSkillAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody("Java", "Advanced")))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String skillBody(String name, String level) {
        return """
                {
                  "name": "%s",
                  "level": "%s"
                }
                """.formatted(name, level);
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