package com.best.cvapp.company.favorite;

import com.best.cvapp.AbstractIntegrationTest;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CompanyFavoriteControllerTest extends AbstractIntegrationTest {

    private static final String TEST_PASSWORD = "Password123!";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String companyToken;

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
    void shouldAddFavoriteSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn409WhenAddingSameFavoriteTwice() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetFavoritesSuccessfully() throws Exception {
        String userToken = registerUserAndGetToken("user@best.com");
        Long cvId = createCV(userToken, "Marko", "Backend developer student");

        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

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

        mockMvc.perform(post("/api/company/cvs/" + cvId + "/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isOk());

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
        mockMvc.perform(delete("/api/company/cvs/999/favorite")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound());
    }

    private String createCompanyAndLogin() throws Exception {
        User companyUser = new User();
        companyUser.setEmail("company@best.com");
        companyUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        companyUser.setRole(Role.COMPANY);
        companyUser.setProvider(AuthProvider.LOCAL);
        companyUser.setEnabled(true);

        userRepository.save(companyUser);

        Company company = new Company();
        company.setUser(companyUser);
        company.setName("BEST Niš");
        company.setDescription("Student organization");
        company.setWebsite("https://best.rs");
        company.setIndustry("Education");

        companyRepository.save(company);

        return loginAndGetToken("company@best.com", TEST_PASSWORD);
    }

    private String registerUserAndGetToken(String email) throws Exception {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        userRepository.save(user);

        return loginAndGetToken(email, TEST_PASSWORD);
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

    private Long createCV(String token, String firstName, String summary) throws Exception {
        String body = """
            {
              "firstName": "%s",
              "lastName": "Milenovic",
              "summary": "%s",
              "location": "Maribor",
              "skills": [],
              "education": [],
              "experience": []
            }
            """.formatted(firstName, summary);

        MvcResult result = mockMvc.perform(put("/api/user/cv")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        return json.get("id").asLong();
    }

    private String extractAccessToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
