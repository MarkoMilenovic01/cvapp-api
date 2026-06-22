package com.best.cvapp.company.upload;

import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyUploadControllerTest {

    private static final String TEST_PASSWORD = "Password123!";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String companyToken;
    private Long companyId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                favorite_cvs, cv_views, companies,
                skills, experience, education, cvs,
                refresh_tokens, users
                RESTART IDENTITY CASCADE
                """);

        companyToken = createCompanyAndLogin();
    }

    @Test
    void shouldUploadCompanyPhotoSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "company.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        when(cloudinaryService.uploadCompanyPhoto(any(MultipartFile.class), eq(companyId)))
                .thenReturn(new CloudinaryService.UploadResult(
                        "https://res.cloudinary.com/test/company.png",
                        "company_photos/1"
                ));

        mockMvc.perform(multipart("/api/company/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test/company.png"));

        Company company = companyRepository.findById(companyId).orElseThrow();

        assert company.getPhotoUrl().equals("https://res.cloudinary.com/test/company.png");
        assert company.getPhotoPublicId().equals("company_photos/1");

        verify(cloudinaryService).uploadCompanyPhoto(any(MultipartFile.class), eq(companyId));
    }

    @Test
    void shouldReplaceExistingCompanyPhotoSuccessfully() throws Exception {
        Company company = companyRepository.findById(companyId).orElseThrow();
        company.setPhotoUrl("https://res.cloudinary.com/test/old.png");
        company.setPhotoPublicId("company_photos/old");
        companyRepository.save(company);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new-company.png",
                MediaType.IMAGE_PNG_VALUE,
                "new-fake-image-content".getBytes()
        );

        when(cloudinaryService.uploadCompanyPhoto(any(MultipartFile.class), eq(companyId)))
                .thenReturn(new CloudinaryService.UploadResult(
                        "https://res.cloudinary.com/test/new.png",
                        "company_photos/new"
                ));

        mockMvc.perform(multipart("/api/company/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/test/new.png"));

        Company updatedCompany = companyRepository.findById(companyId).orElseThrow();

        assert updatedCompany.getPhotoUrl().equals("https://res.cloudinary.com/test/new.png");
        assert updatedCompany.getPhotoPublicId().equals("company_photos/new");

        verify(cloudinaryService).deleteImage("company_photos/old");
        verify(cloudinaryService).uploadCompanyPhoto(any(MultipartFile.class), eq(companyId));
    }

    @Test
    void shouldDeleteCompanyPhotoSuccessfully() throws Exception {
        Company company = companyRepository.findById(companyId).orElseThrow();
        company.setPhotoUrl("https://res.cloudinary.com/test/company.png");
        company.setPhotoPublicId("company_photos/1");
        companyRepository.save(company);

        mockMvc.perform(delete("/api/company/photo")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNoContent());

        Company updatedCompany = companyRepository.findById(companyId).orElseThrow();

        assert updatedCompany.getPhotoUrl() == null;
        assert updatedCompany.getPhotoPublicId() == null;

        verify(cloudinaryService).deleteImage("company_photos/1");
    }

    @Test
    void shouldReturnNoContentWhenDeletingPhotoThatDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/company/photo")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNoContent());

        Company company = companyRepository.findById(companyId).orElseThrow();

        assert company.getPhotoUrl() == null;
        assert company.getPhotoPublicId() == null;

        verify(cloudinaryService, never()).deleteImage(any());
    }

    @Test
    void shouldReturn403WhenUserTriesToUploadCompanyPhoto() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "company.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/company/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenUserTriesToDeleteCompanyPhoto() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(delete("/api/company/photo")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenUploadingCompanyPhotoWithoutAuth() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "company.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/company/photo")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenUploadingPdfAsCompanyPhoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "company.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake-pdf-content".getBytes()
        );

        when(cloudinaryService.uploadCompanyPhoto(any(MultipartFile.class), eq(companyId)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images allowed"));

        mockMvc.perform(multipart("/api/company/photo")
                        .file(file)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUploadingCompanyPhotoWithoutFile() throws Exception {
        mockMvc.perform(multipart("/api/company/photo")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenDeletingCompanyPhotoWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/company/photo"))
                .andExpect(status().isUnauthorized());
    }

    private String createCompanyAndLogin() throws Exception {
        User companyUser = new User();
        companyUser.setEmail("company@best.com");
        companyUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        companyUser.setRole(Role.COMPANY);
        companyUser.setProvider(AuthProvider.LOCAL);

        userRepository.save(companyUser);

        Company company = new Company();
        company.setUser(companyUser);
        company.setName("BEST Niš");
        company.setDescription("Student organization");
        company.setWebsite("https://best.rs");
        company.setIndustry("Education");

        Company savedCompany = companyRepository.save(company);
        companyId = savedCompany.getId();

        return loginAndGetToken("company@best.com", TEST_PASSWORD);
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

    private String loginAndGetToken(String email, String password) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
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