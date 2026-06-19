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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileUploadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE favorite_cvs, cv_views, job_applications, jobs, " +
                        "skills, experience, education, cvs, companies, " +
                        "password_reset_tokens, refresh_tokens, users RESTART IDENTITY CASCADE"
        );
    }

    // ── CV: Profile Photo ─────────────────────────────────────────────────────

    @Test
    void shouldUploadCvProfilePhotoSuccessfully() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        MockMultipartFile photo = mockImageFile();

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(photo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.url").isNotEmpty());
    }

    @Test
    void shouldFailCvPhotoUploadWithoutCv() throws Exception {
        // User has no CV yet
        String token = registerAndGetToken("user@best.com", "123456");

        MockMultipartFile photo = mockImageFile();

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(photo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailCvPhotoUploadWithPdfFile() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        // Sending a PDF where an image is expected
        MockMultipartFile wrongFile = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "fake pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(wrongFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCvPhotoUploadWithNoFile() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCvPhotoUploadWithoutAuth() throws Exception {
        MockMultipartFile photo = mockImageFile();

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(photo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeleteCvProfilePhoto() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        // Upload first
        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(mockImageFile())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Then delete
        mockMvc.perform(delete("/api/user/cv/photo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteCvPhotoWhenNoneExists() throws Exception {
        // Should not throw — just a no-op
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        mockMvc.perform(delete("/api/user/cv/photo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ── CV: PDF ───────────────────────────────────────────────────────────────

    @Test
    void shouldUploadCvPdfSuccessfully() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        MockMultipartFile pdf = mockPdfFile();

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(pdf)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.url").isNotEmpty());
    }

    @Test
    void shouldFailCvPdfUploadWithImageFile() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        // Sending an image where a PDF is expected
        MockMultipartFile wrongFile = mockImageFile();

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(wrongFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCvPdfUploadWithoutCv() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(mockPdfFile())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailCvPdfUploadWithoutAuth() throws Exception {
        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(mockPdfFile()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeleteCvPdf() throws Exception {
        String token = registerAndGetToken("user@best.com", "123456");
        createCv(token);

        // Upload first
        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(mockPdfFile())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Then delete
        mockMvc.perform(delete("/api/user/cv/pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ── Company: Photo ────────────────────────────────────────────────────────

    @Test
    void shouldUploadCompanyPhotoSuccessfully() throws Exception {
        String token = registerCompanyAndGetToken("company@best.com", "123456");

        MockMultipartFile photo = mockImageFile();

        mockMvc.perform(multipart("/api/company/photo")
                        .file(photo)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.url").isNotEmpty());
    }

    @Test
    void shouldFailCompanyPhotoWithoutAuth() throws Exception {
        mockMvc.perform(multipart("/api/company/photo")
                        .file(mockImageFile()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailCompanyPhotoWithPdfFile() throws Exception {
        String token = registerCompanyAndGetToken("company@best.com", "123456");

        MockMultipartFile wrongFile = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "fake pdf content".getBytes()
        );

        mockMvc.perform(multipart("/api/company/photo")
                        .file(wrongFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteCompanyPhoto() throws Exception {
        String token = registerCompanyAndGetToken("company@best.com", "123456");

        // Upload first
        mockMvc.perform(multipart("/api/company/photo")
                        .file(mockImageFile())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Then delete
        mockMvc.perform(delete("/api/company/photo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailUserRoleAccessingCompanyEndpoint() throws Exception {
        // A USER role should not be able to upload a company photo
        String token = registerAndGetToken("user@best.com", "123456");

        mockMvc.perform(multipart("/api/company/photo")
                        .file(mockImageFile())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String registerAndGetToken(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String registerCompanyAndGetToken(String email, String password) throws Exception {
        // Register as USER first, then manually set role to COMPANY in DB
        String token = registerAndGetToken(email, password);

        jdbcTemplate.execute(
                "UPDATE users SET role = 'COMPANY' WHERE email = '" + email + "'"
        );

        // Also create company record
        jdbcTemplate.execute(
                "INSERT INTO companies (user_id, name) " +
                        "VALUES ((SELECT id FROM users WHERE email = '" + email + "'), 'Test Company')"
        );

        // Re-login to get a token with COMPANY role
        return loginAndGetToken(email, password);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private void createCv(String token) throws Exception {
        String body = """
                {
                    "firstName": "Test",
                    "lastName": "User",
                    "phone": "123456789",
                    "address": "Test Address",
                    "summary": "Test summary"
                }
                """;

        mockMvc.perform(put("/api/user/cv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private MockMultipartFile mockImageFile() {
        // Minimal valid 1x1 PNG in bytes
        byte[] minimalPng = new byte[]{
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x02, 0x00, 0x00, 0x00, (byte)0x90, 0x77, 0x53,
                (byte)0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte)0xD7, 0x63, (byte)0xF8, (byte)0xCF, (byte)0xC0, 0x00,
                0x00, 0x00, 0x02, 0x00, 0x01, (byte)0xE2, 0x21, (byte)0xBC,
                0x33, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E,
                0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82
        };
        return new MockMultipartFile("file", "test.png", "image/png", minimalPng);
    }

    private MockMultipartFile mockPdfFile() {
        // Minimal valid PDF bytes
        byte[] minimalPdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj 2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj 3 0 obj<</Type/Page/MediaBox[0 0 612 792]>>endobj\nxref\n0 4\n0000000000 65535 f\ntrailer<</Size 4/Root 1 0 R>>\nstartxref\n9\n%%EOF".getBytes();
        return new MockMultipartFile("file", "test.pdf", "application/pdf", minimalPdf);
    }
}