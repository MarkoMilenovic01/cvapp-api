package com.best.cvapp.config;


import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import com.best.cvapp.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin@cvapp.com", Role.ADMIN);
        seedUser("company@cvapp.com", Role.COMPANY);
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
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("Seeded {} with role {}", email, role);
    }
}