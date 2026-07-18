package com.best.cvapp.cv.skill;

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

class SkillControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Test@1234";
    private static final String SKILLS_URL = "/api/user/cv/skills";

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
                projects, skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        userToken = registerVerifyLoginAndGetAccessToken("user@best.com");
        createCV(userToken);
    }

    @Test
    void shouldGetEmptySkillsListSuccessfully() throws Exception {
        mockMvc.perform(get(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldAddSkillSuccessfully() throws Exception {
        mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.JAVA, SkillLevel.ADVANCED)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("JAVA"))
                .andExpect(jsonPath("$.level").value("ADVANCED"));
    }

    @Test
    void shouldRejectDuplicateSkillForSameCV() throws Exception {
        addSkillAndGetId();

        mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.JAVA, SkillLevel.BEGINNER)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectUpdatingSkillToExistingName() throws Exception {
        addSkillAndGetId();

        MvcResult result = mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.SPRING_BOOT, SkillLevel.INTERMEDIATE)))
                .andExpect(status().isCreated())
                .andReturn();

        Long skillId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(put(SKILLS_URL + "/" + skillId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.JAVA, SkillLevel.INTERMEDIATE)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUpdateSkillSuccessfully() throws Exception {
        Long skillId = addSkillAndGetId();

        mockMvc.perform(put(SKILLS_URL + "/" + skillId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.SPRING_BOOT, SkillLevel.ADVANCED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(skillId))
                .andExpect(jsonPath("$.name").value("SPRING_BOOT"))
                .andExpect(jsonPath("$.level").value("ADVANCED"));
    }

    @Test
    void shouldDeleteSkillSuccessfully() throws Exception {
        Long skillId = addSkillAndGetId();

        mockMvc.perform(delete(SKILLS_URL + "/" + skillId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetAllSkillsSuccessfully() throws Exception {
        addSkillAndGetId();

        mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.SPRING_BOOT, SkillLevel.INTERMEDIATE)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn404WhenSkillNotFound() throws Exception {
        mockMvc.perform(delete(SKILLS_URL + "/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddSkillWithoutCV() throws Exception {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                email_verification_tokens,
                favorite_cvs, cv_views, companies,
                projects, skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        String tokenWithoutCV = registerVerifyLoginAndGetAccessToken("user2@best.com");

        mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + tokenWithoutCV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.JAVA, SkillLevel.ADVANCED)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddSkillWithoutToken() throws Exception {
        mockMvc.perform(post(SKILLS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.JAVA, SkillLevel.ADVANCED)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailAddSkillWithInvalidBody() throws Exception {
        String body = """
                {
                  "name": null,
                  "level": null
                }
                """;

        mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailAddSkillWithInvalidEnumValue() throws Exception {
        String body = """
                {
                  "name": "NOT_A_REAL_SKILL",
                  "level": "ADVANCED"
                }
                """;

        mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private Long addSkillAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post(SKILLS_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillBody(SkillName.JAVA, SkillLevel.ADVANCED)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String skillBody(SkillName name, SkillLevel level) {
        return """
                {
                  "name": "%s",
                  "level": "%s"
                }
                """.formatted(name.name(), level.name());
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
