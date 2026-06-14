package com.best.cvapp;

import com.best.cvapp.auth.dto.RegisterRequest;
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
public class CVControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                skills,
                experience,
                education,
                cvs,
                refresh_tokens,
                users
                RESTART IDENTITY CASCADE
                """);
    }

    // --- Create / Update CV ---

    @Test
    void shouldCreateCVSuccessfully() throws Exception {
        String accessToken = registerAndExtractAccessToken("test@best.com", "123456");

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.phone").value("123456789"))
                .andExpect(jsonPath("$.summary").value("Backend developer student"));
    }

    @Test
    void shouldUpdateCVSuccessfully() throws Exception {
        String accessToken = registerAndExtractAccessToken("test@best.com", "123456");

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson()))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedCvRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.summary").value("Updated summary"));
    }

    // --- Get CV ---

    @Test
    void shouldGetMyCVSuccessfully() throws Exception {
        String accessToken = registerAndExtractAccessToken("test@best.com", "123456");

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"));
    }

    @Test
    void shouldRejectGetCVWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/cv"))
                .andExpect(status().isUnauthorized());
    }

    // --- Delete CV ---

    @Test
    void shouldDeleteCVSuccessfully() throws Exception {
        String accessToken = registerAndExtractAccessToken("test@best.com", "123456");

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/user/cv")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDeleteCVWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/user/cv"))
                .andExpect(status().isUnauthorized());
    }

    // --- Helpers ---

    private String registerAndExtractAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private String cvRequestJson() {
        return """
                {
                  "firstName": "Marko",
                  "lastName": "Milenovic",
                  "phone": "123456789",
                  "address": "Maribor",
                  "summary": "Backend developer student",
                  "linkedinUrl": "https://linkedin.com/in/example",
                  "githubUrl": "https://github.com/example",
                  "education": [
                    {
                      "institution": "University of Maribor",
                      "degree": "Master",
                      "fieldOfStudy": "AI Engineering",
                      "startDate": "2024-10-01",
                      "endDate": "2026-09-30",
                      "current": true
                    }
                  ],
                  "experience": [
                    {
                      "companyName": "BEST Maribor",
                      "position": "Backend Developer",
                      "description": "Worked on CVApp backend.",
                      "startDate": "2025-01-01",
                      "endDate": "2025-09-01",
                      "current": false
                    }
                  ],
                  "skills": [
                    {
                      "name": "Java",
                      "level": "Intermediate"
                    }
                  ]
                }
                """;
    }

    private String updatedCvRequestJson() {
        return """
                {
                  "firstName": "Updated",
                  "lastName": "Milenovic",
                  "phone": "123456789",
                  "address": "Maribor",
                  "summary": "Updated summary",
                  "linkedinUrl": "https://linkedin.com/in/example",
                  "githubUrl": "https://github.com/example",
                  "education": [
                    {
                      "institution": "University of Maribor",
                      "degree": "Master",
                      "fieldOfStudy": "AI Engineering",
                      "startDate": "2024-10-01",
                      "endDate": "2026-09-30",
                      "current": true
                    }
                  ],
                  "experience": [
                    {
                      "companyName": "BEST Maribor",
                      "position": "Backend Developer",
                      "description": "Worked on CVApp backend.",
                      "startDate": "2025-01-01",
                      "endDate": "2025-09-01",
                      "current": false
                    }
                  ],
                  "skills": [
                    {
                      "name": "Java",
                      "level": "Intermediate"
                    }
                  ]
                }
                """;
    }

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}