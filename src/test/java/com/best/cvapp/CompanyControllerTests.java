package com.best.cvapp;

import com.best.cvapp.auth.dto.LoginRequest;
import com.best.cvapp.auth.dto.RegisterRequest;
import com.best.cvapp.company.Company;
import com.best.cvapp.company.CompanyRepository;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CompanyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                favorite_cvs,
                cv_views,
                companies,
                skills,
                experience,
                education,
                cvs,
                refresh_tokens,
                users
                RESTART IDENTITY CASCADE
                """);
    }

    // --- Profile ---

    @Test
    void shouldGetCompanyProfileSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin();

        mockMvc.perform(get("/api/company/me")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BEST Nis"))
                .andExpect(jsonPath("$.description").value("Board of European Students of Technology"))
                .andExpect(jsonPath("$.website").value("https://best.eu.org"))
                .andExpect(jsonPath("$.industry").value("Education"));
    }

    @Test
    void shouldUpdateCompanyProfileSuccessfully() throws Exception {
        String companyToken = createCompanyAndLogin();

        mockMvc.perform(put("/api/company/me")
                        .header("Authorization", "Bearer " + companyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyRequestJson(
                                "BEST Maribor",
                                "Student organization focused on technology and career events",
                                "https://best-maribor.si",
                                "Education and Technology"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BEST Maribor"))
                .andExpect(jsonPath("$.description").value("Student organization focused on technology and career events"))
                .andExpect(jsonPath("$.website").value("https://best-maribor.si"))
                .andExpect(jsonPath("$.industry").value("Education and Technology"));
    }

    // --- CV Browsing ---

    @Test
    void shouldGetAllCVsSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();

        mockMvc.perform(get("/api/company/cvs?page=0&size=10")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Marko"))
                .andExpect(jsonPath("$.content[0].lastName").value("Milenovic"))
                .andExpect(jsonPath("$.content[0].summary").value("Backend developer student"))
                .andExpect(jsonPath("$.content[0].favorite").value(false));
    }

    @Test
    void shouldGetCVByIdSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.firstName").value("Marko"))
                .andExpect(jsonPath("$.lastName").value("Milenovic"))
                .andExpect(jsonPath("$.summary").value("Backend developer student"));
    }

    @Test
    void shouldReturn404WhenCVDoesNotExist() throws Exception {
        String companyToken = createCompanyAndLogin();

        mockMvc.perform(get("/api/company/cvs/999")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    // --- Favorites ---

    @Test
    void shouldAddFavoriteSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();

        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn409WhenAddingSameFavoriteTwice() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();

        addFavorite(companyToken, cvId);

        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetFavoritesSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();
        addFavorite(companyToken, cvId);

        mockMvc.perform(get("/api/company/favorites")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(cvId))
                .andExpect(jsonPath("$[0].firstName").value("Marko"))
                .andExpect(jsonPath("$[0].favorite").value(true));
    }

    @Test
    void shouldRemoveFavoriteSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();
        addFavorite(companyToken, cvId);

        mockMvc.perform(delete("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/company/favorites")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturn404WhenRemovingNonExistingFavorite() throws Exception {
        String companyToken = createCompanyAndLogin();

        mockMvc.perform(delete("/api/company/cvs/999/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    // --- History ---

    @Test
    void shouldGetViewHistorySuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/company/history")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cvId").value(cvId))
                .andExpect(jsonPath("$[0].firstName").value("Marko"))
                .andExpect(jsonPath("$[0].lastName").value("Milenovic"))
                .andExpect(jsonPath("$[0].viewedAt").exists());
    }

    @Test
    void shouldKeepHistoryDistinctForSameCV() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        String companyToken = createCompanyAndLogin();

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/company/cvs/" + cvId)
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/company/history")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cvId").value(cvId));
    }

    // --- Authorization ---

    @Test
    void shouldRejectCompanyEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/company/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUserTokenOnCompanyEndpoint() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");

        mockMvc.perform(get("/api/company/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // --- Helpers ---

    private String createCompanyAndLogin() throws Exception {
        User user = User.builder()
                .email("company@best.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.COMPANY)
                .enabled(true)
                .build();

        userRepository.save(user);

        Company company = Company.builder()
                .user(user)
                .name("BEST Nis")
                .description("Board of European Students of Technology")
                .website("https://best.eu.org")
                .industry("Education")
                .build();

        companyRepository.save(company);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest("company@best.com", "password"))))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result);
    }

    private String registerUserAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest(email, "123456"))))
                .andExpect(status().isOk())
                .andReturn();

        return extractAccessToken(result);
    }

    private Long createCV(String token, String firstName, String summary) throws Exception {
        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cvRequestJson(firstName, summary)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    private void addFavorite(String companyToken, Long cvId) throws Exception {
        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private String companyRequestJson(String name, String description, String website, String industry) {
        return """
                {
                    "name": "%s",
                    "description": "%s",
                    "website": "%s",
                    "industry": "%s"
                }
                """.formatted(name, description, website, industry);
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

    private String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}