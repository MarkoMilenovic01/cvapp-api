package com.best.cvapp.cv.upload;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.credentials.dto.LoginRequest;
import com.best.cvapp.auth.credentials.dto.RegisterRequest;
import com.best.cvapp.auth.emailverification.dto.VerifyEmailRequest;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CVUploadControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Test@1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CVRepository cvRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userToken;
    private Long cvId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                email_verification_tokens,
                favorite_cvs, cv_views, companies,
                skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        userToken = registerVerifyLoginAndGetAccessToken("user@best.com");
        cvId = createCV(userToken);
    }

    @Test
    void shouldUploadCvProfilePhotoSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        when(cloudinaryService.uploadCvProfilePhoto(any(MultipartFile.class), eq(cvId)))
                .thenReturn(new CloudinaryService.UploadResult(
                        "https://res.cloudinary.com/test/profile.png",
                        "cv_photos/1"
                ));

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test/profile.png"));

        CV cv = cvRepository.findById(cvId).orElseThrow();

        assertThat(cv.getProfilePhotoUrl()).isEqualTo("https://res.cloudinary.com/test/profile.png");
        assertThat(cv.getProfilePhotoId()).isEqualTo("cv_photos/1");

        verify(cloudinaryService).uploadCvProfilePhoto(any(MultipartFile.class), eq(cvId));
    }

    @Test
    void shouldReplaceExistingCvProfilePhotoSuccessfully() throws Exception {
        CV cv = cvRepository.findById(cvId).orElseThrow();
        cv.setProfilePhotoUrl("https://res.cloudinary.com/test/old-profile.png");
        cv.setProfilePhotoId("cv_photos/1");
        cvRepository.save(cv);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new-profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "new-fake-image-content".getBytes()
        );

        when(cloudinaryService.uploadCvProfilePhoto(any(MultipartFile.class), eq(cvId)))
                .thenReturn(new CloudinaryService.UploadResult(
                        "https://res.cloudinary.com/test/new-profile.png",
                        "cv_photos/1"
                ));

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test/new-profile.png"));

        CV updatedCv = cvRepository.findById(cvId).orElseThrow();

        assertThat(updatedCv.getProfilePhotoUrl()).isEqualTo("https://res.cloudinary.com/test/new-profile.png");
        assertThat(updatedCv.getProfilePhotoId()).isEqualTo("cv_photos/1");

        verify(cloudinaryService, never()).deleteImage(anyString());
        verify(cloudinaryService).uploadCvProfilePhoto(any(MultipartFile.class), eq(cvId));
    }

    @Test
    void shouldDeleteCvProfilePhotoSuccessfully() throws Exception {
        CV cv = cvRepository.findById(cvId).orElseThrow();
        cv.setProfilePhotoUrl("https://res.cloudinary.com/test/profile.png");
        cv.setProfilePhotoId("cv_photos/1");
        cvRepository.save(cv);

        mockMvc.perform(delete("/api/user/cv/photo")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        CV updatedCv = cvRepository.findById(cvId).orElseThrow();

        assertThat(updatedCv.getProfilePhotoUrl()).isNull();
        assertThat(updatedCv.getProfilePhotoId()).isNull();

        verify(cloudinaryService).deleteImage("cv_photos/1");
    }

    @Test
    void shouldReturnNoContentWhenDeletingPhotoThatDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/user/cv/photo")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        verify(cloudinaryService, never()).deleteImage(anyString());
    }

    @Test
    void shouldUploadCvPdfSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake-pdf-content".getBytes()
        );

        when(cloudinaryService.uploadCvPdf(any(MultipartFile.class), eq(cvId)))
                .thenReturn(new CloudinaryService.UploadResult(
                        "https://res.cloudinary.com/test/cv.pdf",
                        "cv_pdfs/1"
                ));

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test/cv.pdf"));

        CV cv = cvRepository.findById(cvId).orElseThrow();

        assertThat(cv.getPdfUrl()).isEqualTo("https://res.cloudinary.com/test/cv.pdf");
        assertThat(cv.getPdfPublicId()).isEqualTo("cv_pdfs/1");

        verify(cloudinaryService).uploadCvPdf(any(MultipartFile.class), eq(cvId));
    }

    @Test
    void shouldReplaceExistingCvPdfSuccessfully() throws Exception {
        CV cv = cvRepository.findById(cvId).orElseThrow();
        cv.setPdfUrl("https://res.cloudinary.com/test/old-cv.pdf");
        cv.setPdfPublicId("cv_pdfs/1");
        cvRepository.save(cv);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new-cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "new-fake-pdf-content".getBytes()
        );

        when(cloudinaryService.uploadCvPdf(any(MultipartFile.class), eq(cvId)))
                .thenReturn(new CloudinaryService.UploadResult(
                        "https://res.cloudinary.com/test/new-cv.pdf",
                        "cv_pdfs/1"
                ));

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test/new-cv.pdf"));

        CV updatedCv = cvRepository.findById(cvId).orElseThrow();

        assertThat(updatedCv.getPdfUrl()).isEqualTo("https://res.cloudinary.com/test/new-cv.pdf");
        assertThat(updatedCv.getPdfPublicId()).isEqualTo("cv_pdfs/1");

        verify(cloudinaryService, never()).deletePdf(anyString());
        verify(cloudinaryService).uploadCvPdf(any(MultipartFile.class), eq(cvId));
    }

    @Test
    void shouldDeleteCvPdfSuccessfully() throws Exception {
        CV cv = cvRepository.findById(cvId).orElseThrow();
        cv.setPdfUrl("https://res.cloudinary.com/test/cv.pdf");
        cv.setPdfPublicId("cv_pdfs/1");
        cvRepository.save(cv);

        mockMvc.perform(delete("/api/user/cv/pdf")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        CV updatedCv = cvRepository.findById(cvId).orElseThrow();

        assertThat(updatedCv.getPdfUrl()).isNull();
        assertThat(updatedCv.getPdfPublicId()).isNull();

        verify(cloudinaryService).deletePdf("cv_pdfs/1");
    }

    @Test
    void shouldReturnNoContentWhenDeletingPdfThatDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/user/cv/pdf")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        verify(cloudinaryService, never()).deletePdf(anyString());
    }

    @Test
    void shouldReturn404WhenUploadingCvPhotoWithoutCV() throws Exception {
        String tokenWithoutCv = registerVerifyLoginAndGetAccessToken("nocv@best.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + tokenWithoutCv))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenUploadingCvPhotoWithoutFile() throws Exception {
        mockMvc.perform(multipart("/api/user/cv/photo")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenUploadingCvPhotoWithoutAuth() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenUploadingPdfAsCvPhoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake-pdf-content".getBytes()
        );

        when(cloudinaryService.uploadCvProfilePhoto(any(MultipartFile.class), eq(cvId)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images allowed"));

        mockMvc.perform(multipart("/api/user/cv/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUploadingCvPdfWithoutCV() throws Exception {
        String tokenWithoutCv = registerVerifyLoginAndGetAccessToken("nocvpdf@best.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake-pdf-content".getBytes()
        );

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(file)
                        .header("Authorization", "Bearer " + tokenWithoutCv))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenUploadingCvPdfWithoutAuth() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake-pdf-content".getBytes()
        );

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenUploadingImageAsCvPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        when(cloudinaryService.uploadCvPdf(any(MultipartFile.class), eq(cvId)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDFs allowed"));

        mockMvc.perform(multipart("/api/user/cv/pdf")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    private Long createCV(String token) throws Exception {
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

        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
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
