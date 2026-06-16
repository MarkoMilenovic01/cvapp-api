package com.best.cvapp.shared.seed;

import com.best.cvapp.company.Company;
import com.best.cvapp.company.CompanyRepository;
import com.best.cvapp.cv.CV;
import com.best.cvapp.cv.CVRepository;
import com.best.cvapp.job.*;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CVRepository cvRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = seedUser("admin@cvapp.com", "password", Role.ADMIN);
        User companyUser = seedUser("company@cvapp.com", "password", Role.COMPANY);
        User companyUser2 = seedUser("company2@cvapp.com", "password", Role.COMPANY);
        User normalUser = seedUser("user@cvapp.com", "password", Role.USER);
        User normalUser2 = seedUser("user2@cvapp.com", "password", Role.USER);

        Company company = seedCompany(
                companyUser,
                "BEST Nis",
                "Board of European Students of Technology",
                "https://best.eu.org",
                "Education"
        );

        Company company2 = seedCompany(
                companyUser2,
                "Google",
                "Technology company focused on software, cloud and AI.",
                "https://google.com",
                "Technology"
        );

        CV cv1 = seedCV(
                normalUser,
                "Marko",
                "Milenovic",
                "123456789",
                "Maribor",
                "Backend developer student interested in Java, Spring Boot, PostgreSQL and AI.",
                "https://linkedin.com/in/example",
                "https://github.com/example"
        );

        CV cv2 = seedCV(
                normalUser2,
                "Ana",
                "Petrovic",
                "987654321",
                "Ljubljana",
                "Frontend developer student interested in React, TypeScript and UI development.",
                "https://linkedin.com/in/ana-example",
                "https://github.com/ana-example"
        );

        Job backendJob = seedJob(
                company,
                "Backend Developer Intern",
                "We are looking for a motivated backend developer intern to help build REST APIs and backend services.",
                "Java, Spring Boot, PostgreSQL, Git, Docker basics",
                "Maribor, Slovenia",
                EmploymentType.INTERNSHIP,
                WorkMode.HYBRID,
                LocalDate.of(2026, 7, 31)
        );

        Job frontendJob = seedJob(
                company,
                "Frontend Developer Student",
                "We are looking for a student frontend developer to work on web application interfaces.",
                "React, TypeScript, HTML, CSS, Git",
                "Ljubljana, Slovenia",
                EmploymentType.STUDENT_WORK,
                WorkMode.REMOTE,
                LocalDate.of(2026, 8, 15)
        );

        Job aiJob = seedJob(
                company2,
                "AI/ML Intern",
                "We are looking for an AI/ML intern to help with data processing, model experiments, and AI prototypes.",
                "Python, Machine Learning, Deep Learning basics, Git",
                "Maribor, Slovenia",
                EmploymentType.INTERNSHIP,
                WorkMode.ONSITE,
                LocalDate.of(2026, 9, 1)
        );

        seedApplication(backendJob, normalUser, cv1, ApplicationStatus.APPLIED);
        seedApplication(frontendJob, normalUser2, cv2, ApplicationStatus.SHORTLISTED);

        log.info("Database seeding finished.");
        log.info("Admin login: admin@cvapp.com / password");
        log.info("Company login: company@cvapp.com / password");
        log.info("Second company login: company2@cvapp.com / password");
        log.info("User login: user@cvapp.com / password");
        log.info("Second user login: user2@cvapp.com / password");
    }

    private User seedUser(String email, String password, Role role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(password))
                            .role(role)
                            .enabled(true)
                            .build();

                    User saved = userRepository.save(user);
                    log.info("Seeded user {} with role {}", email, role);
                    return saved;
                });
    }

    private Company seedCompany(
            User user,
            String name,
            String description,
            String website,
            String industry
    ) {
        return companyRepository.findByUser(user)
                .orElseGet(() -> {
                    Company company = Company.builder()
                            .user(user)
                            .name(name)
                            .description(description)
                            .website(website)
                            .industry(industry)
                            .build();

                    Company saved = companyRepository.save(company);
                    log.info("Seeded company {}", name);
                    return saved;
                });
    }

    private CV seedCV(
            User user,
            String firstName,
            String lastName,
            String phone,
            String address,
            String summary,
            String linkedinUrl,
            String githubUrl
    ) {
        return cvRepository.findByUser(user)
                .orElseGet(() -> {
                    CV cv = CV.builder()
                            .user(user)
                            .firstName(firstName)
                            .lastName(lastName)
                            .phone(phone)
                            .address(address)
                            .summary(summary)
                            .linkedinUrl(linkedinUrl)
                            .githubUrl(githubUrl)
                            .build();

                    CV saved = cvRepository.save(cv);
                    log.info("Seeded CV for {} {}", firstName, lastName);
                    return saved;
                });
    }

    private Job seedJob(
            Company company,
            String title,
            String description,
            String requirements,
            String location,
            EmploymentType employmentType,
            WorkMode workMode,
            LocalDate deadline
    ) {
        return jobRepository.findByCompany(company, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(job -> job.getTitle().equals(title))
                .findFirst()
                .orElseGet(() -> {
                    Job job = Job.builder()
                            .company(company)
                            .title(title)
                            .description(description)
                            .requirements(requirements)
                            .location(location)
                            .employmentType(employmentType)
                            .workMode(workMode)
                            .deadline(deadline)
                            .active(true)
                            .build();

                    Job saved = jobRepository.save(job);
                    log.info("Seeded job {}", title);
                    return saved;
                });
    }

    private void seedApplication(Job job, User user, CV cv, ApplicationStatus status) {
        if (jobApplicationRepository.existsByJobAndUser(job, user)) {
            log.info("Skipping application for user {} and job {} — already exists", user.getEmail(), job.getTitle());
            return;
        }

        JobApplication application = JobApplication.builder()
                .job(job)
                .user(user)
                .cv(cv)
                .status(status)
                .build();

        jobApplicationRepository.save(application);
        log.info("Seeded application: user {} applied to {}", user.getEmail(), job.getTitle());
    }
}