package com.best.cvapp.cv.profile;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.emailverification.dto.VerifyEmailRequest;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verify;

class CVControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Test@1234";
    private static final String CV_URL = "/api/user/cv";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private CloudinaryService cloudinaryService;

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
    }

    @Test
    void shouldCreateCVSuccessfully() throws Exception {
        mockMvc.perform(put(CV_URL)
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
                .andExpect(jsonPath("$.experience.length()").value(0))
                .andExpect(jsonPath("$.projects.length()").value(0))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.profilePhotoUrl").doesNotExist())
                .andExpect(jsonPath("$.pdfUrl").doesNotExist());
    }

    @Test
    void shouldGetMyCVSuccessfully() throws Exception {
        Long cvId = createCV(userToken, "Marko", "Milenovic", "Backend developer student");

        mockMvc.perform(get(CV_URL)
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

        mockMvc.perform(put(CV_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvBody("Marko", "Milenovic", "AI engineer student")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.summary").value("AI engineer student"));

        mockMvc.perform(get(CV_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.summary").value("AI engineer student"));
    }

    @Test
    void shouldDeleteCVSuccessfully() throws Exception {
        Long cvId = createCV(userToken, "Marko", "Milenovic", "Backend developer student");
        jdbcTemplate.update("""
                UPDATE cvs
                SET profile_photo_id = ?, pdf_public_id = ?
                WHERE id = ?
                """, "cv_photos/1", "cv_pdfs/1", cvId);

        mockMvc.perform(delete(CV_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(CV_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());

        verify(cloudinaryService).deleteImage("cv_photos/1");
        verify(cloudinaryService).deletePdf("cv_pdfs/1");
    }

    @Test
    void shouldReturn404WhenCVNotFound() throws Exception {
        mockMvc.perform(get(CV_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectGetCVWithoutToken() throws Exception {
        mockMvc.perform(get(CV_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectCreateCVWithoutToken() throws Exception {
        mockMvc.perform(put(CV_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvBody("Marko", "Milenovic", "Backend developer student")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentCV() throws Exception {
        mockMvc.perform(delete(CV_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailCreateCVWithInvalidBody() throws Exception {
        String body = """
                {
                  "firstName": "%s",
                  "lastName": "Milenovic",
                  "phone": "+38612345678",
                  "address": "Maribor",
                  "summary": "Backend developer student",
                  "linkedinUrl": "https://linkedin.com/in/marko",
                  "githubUrl": "https://github.com/marko"
                }
                """.formatted("a".repeat(101));

        mockMvc.perform(put(CV_URL)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private Long createCV(String token, String firstName, String lastName, String summary) throws Exception {
        MvcResult result = mockMvc.perform(put(CV_URL)
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
                  "githubUrl": "https://github.com/marko"
                }
                """.formatted(firstName, lastName, summary);
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
