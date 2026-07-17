package com.best.cvapp.cv.education;

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

class EducationControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Test@1234";
    private static final String EDUCATION_URL = "/api/user/cv/education";

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
    void shouldGetEmptyEducationListSuccessfully() throws Exception {
        mockMvc.perform(get(EDUCATION_URL)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldAddEducationSuccessfully() throws Exception {
        mockMvc.perform(post(EDUCATION_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody(
                                "University of Maribor",
                                "Master",
                                "AI Engineering",
                                true
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.institution").value("University of Maribor"))
                .andExpect(jsonPath("$.degree").value("Master"))
                .andExpect(jsonPath("$.fieldOfStudy").value("AI Engineering"))
                .andExpect(jsonPath("$.startDate").value("2024-10-01"))
                .andExpect(jsonPath("$.endDate").doesNotExist())
                .andExpect(jsonPath("$.current").value(true));
    }

    @Test
    void shouldUpdateEducationSuccessfully() throws Exception {
        Long educationId = addEducationAndGetId();

        mockMvc.perform(put(EDUCATION_URL + "/" + educationId)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody(
                                "FERI",
                                "MSc",
                                "Artificial Intelligence",
                                false
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(educationId))
                .andExpect(jsonPath("$.institution").value("FERI"))
                .andExpect(jsonPath("$.degree").value("MSc"))
                .andExpect(jsonPath("$.fieldOfStudy").value("Artificial Intelligence"))
                .andExpect(jsonPath("$.startDate").value("2024-10-01"))
                .andExpect(jsonPath("$.current").value(false));
    }

    @Test
    void shouldDeleteEducationSuccessfully() throws Exception {
        Long educationId = addEducationAndGetId();

        mockMvc.perform(delete(EDUCATION_URL + "/" + educationId)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(EDUCATION_URL)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetAllEducationSuccessfully() throws Exception {
        addEducationAndGetId();

        mockMvc.perform(post(EDUCATION_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody(
                                "University of Nis",
                                "Bachelor",
                                "Computer Science",
                                false
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(get(EDUCATION_URL)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn404WhenEducationNotFound() throws Exception {
        mockMvc.perform(delete(EDUCATION_URL + "/999")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddEducationWithoutCV() throws Exception {
        truncateDatabase();

        String tokenWithoutCV = registerVerifyLoginAndGetAccessToken("user2@best.com");

        mockMvc.perform(post(EDUCATION_URL)
                        .header("Authorization", bearer(tokenWithoutCV))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody(
                                "University of Maribor",
                                "Master",
                                "AI Engineering",
                                true
                        )))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailAddEducationWithoutToken() throws Exception {
        mockMvc.perform(post(EDUCATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody(
                                "University of Maribor",
                                "Master",
                                "AI Engineering",
                                true
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailAddEducationWithInvalidBody() throws Exception {
        String body = """
                {
                  "institution": "",
                  "degree": "",
                  "fieldOfStudy": "",
                  "startDate": "2024-10-01",
                  "endDate": null,
                  "current": true
                }
                """;

        mockMvc.perform(post(EDUCATION_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private Long addEducationAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post(EDUCATION_URL)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(educationBody(
                                "University of Maribor",
                                "Master",
                                "AI Engineering",
                                true
                        )))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private String educationBody(
            String institution,
            String degree,
            String fieldOfStudy,
            boolean current
    ) {
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
                skills, experience, education, cvs,
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
                        .content(toJson(new RegisterRequest(
                                email,
                                TEST_PASSWORD,
                                TEST_PASSWORD
                        ))))
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
