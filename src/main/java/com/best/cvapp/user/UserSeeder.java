package com.best.cvapp.user;


import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin@cvapp.com", Role.ADMIN);
        seedCompany();
        seedUser("user@cvapp.com", Role.USER);
    }

    private void seedUser(String email, Role role) {
        if (userRepository.existsByEmail(email)) {
            log.info("Skipping seed for {} — already exists", email);
            return;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password"))
                .role(role)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("Seeded {} with role {}", email, role);
    }

    private void seedCompany() {
        if (userRepository.existsByEmail("company@cvapp.com")) {
            log.info("Skipping seed for company@cvapp.com — already exists");
            return;
        }

        User user = User.builder()
                .email("company@cvapp.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.COMPANY)
                .provider(AuthProvider.LOCAL)
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
        log.info("Seeded company@cvapp.com with company BEST Nis");
    }

}