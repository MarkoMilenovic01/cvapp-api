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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        String token = registerAndGetToken();

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson("Marko", "Backend developer student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.phone").value("123456789"))
                .andExpect(jsonPath("$.summary").value("Backend developer student"));
    }

    @Test
    void shouldUpdateCVSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson("Updated", "Updated summary")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.summary").value("Updated summary"));
    }

    // --- Get CV ---

    @Test
    void shouldGetMyCVSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"));
    }

    @Test
    void shouldReturn404WhenCVNotFound() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/api/user/cv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectGetCVWithoutToken() throws Exception {
        mockMvc.perform(get("/api/user/cv"))
                .andExpect(status().isUnauthorized());
    }

    // --- Delete CV ---

    @Test
    void shouldDeleteCVSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(delete("/api/user/cv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentCV() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(delete("/api/user/cv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // --- Education ---

    @Test
    void shouldAddEducationSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(post("/api/user/cv/education")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson("University of Maribor", "Master", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution").value("University of Maribor"))
                .andExpect(jsonPath("$.degree").value("Master"))
                .andExpect(jsonPath("$.current").value(true));
    }

    @Test
    void shouldGetAllEducationSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        addEducation(token, "University of Maribor", "Master", true);
        addEducation(token, "University of Nis", "Bachelor", false);

        mockMvc.perform(get("/api/user/cv/education")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateEducationSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        Long id = addEducation(token, "University of Maribor", "Master", true);

        mockMvc.perform(put("/api/user/cv/education/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson("University of Ljubljana", "PhD", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution").value("University of Ljubljana"))
                .andExpect(jsonPath("$.degree").value("PhD"))
                .andExpect(jsonPath("$.current").value(false));
    }

    @Test
    void shouldDeleteEducationSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        Long id = addEducation(token, "University of Maribor", "Master", true);

        mockMvc.perform(delete("/api/user/cv/education/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenEducationNotFound() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(delete("/api/user/cv/education/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddEducationWithoutCV() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(post("/api/user/cv/education")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson("University of Maribor", "Master", true)))
                .andExpect(status().isNotFound());
    }

    // --- Experience ---

    @Test
    void shouldAddExperienceSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceJson("BEST Maribor", "Backend Developer", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("BEST Maribor"))
                .andExpect(jsonPath("$.position").value("Backend Developer"))
                .andExpect(jsonPath("$.current").value(true));
    }

    @Test
    void shouldGetAllExperienceSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        addExperience(token, "BEST Maribor", "Backend Developer", true);
        addExperience(token, "Google", "Software Engineer", false);

        mockMvc.perform(get("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateExperienceSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        Long id = addExperience(token, "BEST Maribor", "Backend Developer", true);

        mockMvc.perform(put("/api/user/cv/experience/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceJson("Google", "Senior Engineer", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Google"))
                .andExpect(jsonPath("$.position").value("Senior Engineer"))
                .andExpect(jsonPath("$.current").value(false));
    }

    @Test
    void shouldDeleteExperienceSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        Long id = addExperience(token, "BEST Maribor", "Backend Developer", true);

        mockMvc.perform(delete("/api/user/cv/experience/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenExperienceNotFound() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(delete("/api/user/cv/experience/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddExperienceWithoutCV() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceJson("BEST Maribor", "Backend Developer", true)))
                .andExpect(status().isNotFound());
    }

    // --- Skills ---

    @Test
    void shouldAddSkillSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson("Java", "ADVANCED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.level").value("ADVANCED"));
    }

    @Test
    void shouldGetAllSkillsSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        addSkill(token, "Java", "ADVANCED");
        addSkill(token, "Spring Boot", "INTERMEDIATE");

        mockMvc.perform(get("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateSkillSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        Long id = addSkill(token, "Java", "BEGINNER");

        mockMvc.perform(put("/api/user/cv/skills/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson("Java", "ADVANCED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.level").value("ADVANCED"));
    }

    @Test
    void shouldDeleteSkillSuccessfully() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");
        Long id = addSkill(token, "Java", "ADVANCED");

        mockMvc.perform(delete("/api/user/cv/skills/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenSkillNotFound() throws Exception {
        String token = registerAndGetToken();
        createCV(token, "Marko", "Backend developer student");

        mockMvc.perform(delete("/api/user/cv/skills/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddSkillWithoutCV() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson("Java", "ADVANCED")))
                .andExpect(status().isNotFound());
    }

    // --- Helpers ---

    private String registerAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest("test@best.com", "123456"))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private void createCV(String token, String firstName, String summary) throws Exception {
        mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson(firstName, summary)))
                .andExpect(status().isOk());
    }

    private Long addEducation(String token, String institution, String degree, boolean current) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/cv/education")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationJson(institution, degree, current)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private Long addExperience(String token, String company, String position, boolean current) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/cv/experience")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(experienceJson(company, position, current)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private Long addSkill(String token, String name, String level) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/user/cv/skills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson(name, level)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);
        return request;
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

    private String educationJson(String institution, String degree, boolean current) {
        return """
                {
                    "institution": "%s",
                    "degree": "%s",
                    "fieldOfStudy": "Computer Science",
                    "startDate": "2020-09-01",
                    "endDate": null,
                    "current": %b
                }
                """.formatted(institution, degree, current);
    }

    private String experienceJson(String company, String position, boolean current) {
        return """
                {
                    "companyName": "%s",
                    "position": "%s",
                    "description": "Worked on backend",
                    "startDate": "2023-01-01",
                    "endDate": null,
                    "current": %b
                }
                """.formatted(company, position, current);
    }

    private String skillJson(String name, String level) {
        return """
                {
                    "name": "%s",
                    "level": "%s"
                }
                """.formatted(name, level);
    }

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}