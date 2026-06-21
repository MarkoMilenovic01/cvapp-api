package com.best.cvapp.shared.seed;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.cv.education.Education;
import com.best.cvapp.cv.experience.Experience;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.skill.Skill;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.job.application.JobApplication;
import com.best.cvapp.job.application.JobApplicationRepository;
import com.best.cvapp.job.core.*;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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

        // ── Users ─────────────────────────────────────────────────────────────
        User admin      = seedUser("admin@cvapp.com",    "password", Role.ADMIN);
        User companyU1  = seedUser("company@cvapp.com",  "password", Role.COMPANY);
        User companyU2  = seedUser("company2@cvapp.com", "password", Role.COMPANY);
        User companyU3  = seedUser("company3@cvapp.com", "password", Role.COMPANY);
        User user1      = seedUser("user@cvapp.com",     "password", Role.USER);
        User user2      = seedUser("user2@cvapp.com",    "password", Role.USER);
        User user3      = seedUser("user3@cvapp.com",    "password", Role.USER);
        User user4      = seedUser("user4@cvapp.com",    "password", Role.USER);

        // ── Companies ─────────────────────────────────────────────────────────
        Company bestNis = seedCompany(companyU1,
                "BEST Nis",
                "Board of European Students of Technology, Nis chapter.",
                "https://best.eu.org",
                "Education");

        Company google = seedCompany(companyU2,
                "Google",
                "Technology company focused on software, cloud and AI.",
                "https://google.com",
                "Technology");

        Company infostud = seedCompany(companyU3,
                "Infostud",
                "Leading Serbian HR and job platform company.",
                "https://infostud.com",
                "HR Technology");

        // ── CVs ───────────────────────────────────────────────────────────────
        CV cv1 = seedCV(
                user1, "Marko", "Milenovic", "0641234567", "Nis, Serbia",
                "Backend developer student passionate about Java, Spring Boot, PostgreSQL and cloud architecture.",
                "https://linkedin.com/in/marko-milenovic",
                "https://github.com/marko-milenovic",
                List.of(
                        new SkillSeed("Java", "ADVANCED"),
                        new SkillSeed("Spring Boot", "INTERMEDIATE"),
                        new SkillSeed("PostgreSQL", "INTERMEDIATE"),
                        new SkillSeed("Docker", "BEGINNER"),
                        new SkillSeed("Git", "ADVANCED")
                ),
                List.of(
                        new EducationSeed(
                                "University of Nis — Faculty of Electronic Engineering",
                                "Bachelor of Science",
                                "Computer Science",
                                LocalDate.of(2021, 10, 1),
                                null,
                                true)
                ),
                List.of(
                        new ExperienceSeed(
                                "StartupX",
                                "Junior Backend Developer",
                                "Built REST APIs using Spring Boot, managed PostgreSQL schemas and wrote unit tests.",
                                LocalDate.of(2023, 6, 1),
                                LocalDate.of(2023, 9, 30),
                                false)
                )
        );

        CV cv2 = seedCV(
                user2, "Ana", "Petrovic", "0697654321", "Ljubljana, Slovenia",
                "Frontend developer student focused on React, TypeScript and accessible UI design.",
                "https://linkedin.com/in/ana-petrovic",
                "https://github.com/ana-petrovic",
                List.of(
                        new SkillSeed("React", "ADVANCED"),
                        new SkillSeed("TypeScript", "INTERMEDIATE"),
                        new SkillSeed("CSS", "ADVANCED"),
                        new SkillSeed("Figma", "INTERMEDIATE"),
                        new SkillSeed("Git", "INTERMEDIATE")
                ),
                List.of(
                        new EducationSeed(
                                "University of Ljubljana — Faculty of Computer Science",
                                "Bachelor of Science",
                                "Software Engineering",
                                LocalDate.of(2022, 10, 1),
                                null,
                                true)
                ),
                List.of(
                        new ExperienceSeed(
                                "Creative Agency d.o.o.",
                                "Frontend Intern",
                                "Developed responsive landing pages and component libraries in React.",
                                LocalDate.of(2024, 2, 1),
                                LocalDate.of(2024, 5, 31),
                                false)
                )
        );

        CV cv3 = seedCV(
                user3, "Stefan", "Jovanovic", "0621112233", "Belgrade, Serbia",
                "Full-stack developer with experience in React and Node.js, currently exploring cloud-native development.",
                "https://linkedin.com/in/stefan-jovanovic",
                "https://github.com/stefan-jovanovic",
                List.of(
                        new SkillSeed("React", "ADVANCED"),
                        new SkillSeed("Node.js", "ADVANCED"),
                        new SkillSeed("MongoDB", "INTERMEDIATE"),
                        new SkillSeed("AWS", "BEGINNER"),
                        new SkillSeed("Git", "ADVANCED")
                ),
                List.of(
                        new EducationSeed(
                                "University of Belgrade — School of Electrical Engineering",
                                "Bachelor of Science",
                                "Software Engineering",
                                LocalDate.of(2020, 10, 1),
                                LocalDate.of(2024, 6, 30),
                                false)
                ),
                List.of(
                        new ExperienceSeed(
                                "Levi9",
                                "Associate Software Engineer",
                                "Worked on full-stack features for enterprise clients using React and Node.js microservices.",
                                LocalDate.of(2023, 9, 1),
                                null,
                                true)
                )
        );

        CV cv4 = seedCV(
                user4, "Milica", "Nikolic", "0638887766", "Novi Sad, Serbia",
                "Data science student with a strong interest in machine learning, Python and data visualization.",
                "https://linkedin.com/in/milica-nikolic",
                "https://github.com/milica-nikolic",
                List.of(
                        new SkillSeed("Python", "ADVANCED"),
                        new SkillSeed("Machine Learning", "INTERMEDIATE"),
                        new SkillSeed("Pandas", "ADVANCED"),
                        new SkillSeed("TensorFlow", "BEGINNER"),
                        new SkillSeed("SQL", "INTERMEDIATE")
                ),
                List.of(
                        new EducationSeed(
                                "University of Novi Sad — Faculty of Sciences",
                                "Bachelor of Science",
                                "Data Science",
                                LocalDate.of(2021, 10, 1),
                                null,
                                true)
                ),
                List.of(
                        new ExperienceSeed(
                                "DataLab Research",
                                "ML Research Intern",
                                "Assisted in building and evaluating classification models for NLP tasks using Python and TensorFlow.",
                                LocalDate.of(2024, 3, 1),
                                LocalDate.of(2024, 6, 30),
                                false)
                )
        );

        // ── Jobs ──────────────────────────────────────────────────────────────
        Job backendJob = seedJob(bestNis,
                "Backend Developer Intern",
                "We are looking for a motivated backend developer intern to help build REST APIs and backend services.",
                "Java, Spring Boot, PostgreSQL, Git, Docker basics",
                "Nis, Serbia",
                EmploymentType.INTERNSHIP, WorkMode.HYBRID,
                LocalDate.of(2026, 7, 31));

        Job frontendJob = seedJob(bestNis,
                "Frontend Developer Student",
                "We are looking for a student frontend developer to work on web application interfaces.",
                "React, TypeScript, HTML, CSS, Git",
                "Ljubljana, Slovenia",
                EmploymentType.STUDENT_WORK, WorkMode.REMOTE,
                LocalDate.of(2026, 8, 15));

        Job aiJob = seedJob(google,
                "AI/ML Intern",
                "We are looking for an AI/ML intern to help with data processing, model experiments and AI prototypes.",
                "Python, Machine Learning, Deep Learning basics, Git",
                "Belgrade, Serbia",
                EmploymentType.INTERNSHIP, WorkMode.ONSITE,
                LocalDate.of(2026, 9, 1));

        Job fullstackJob = seedJob(google,
                "Full-Stack Engineer Student",
                "Join our team as a student full-stack engineer working on internal tools and dashboards.",
                "React, Node.js, REST APIs, Git, basic cloud knowledge",
                "Novi Sad, Serbia",
                EmploymentType.STUDENT_WORK, WorkMode.HYBRID,
                LocalDate.of(2026, 8, 1));

        Job devopsJob = seedJob(infostud,
                "DevOps Intern",
                "Help us maintain and improve our CI/CD pipelines and cloud infrastructure.",
                "Linux, Docker, GitHub Actions, basic AWS or GCP",
                "Belgrade, Serbia",
                EmploymentType.INTERNSHIP, WorkMode.ONSITE,
                LocalDate.of(2026, 7, 15));

        Job dataJob = seedJob(infostud,
                "Data Analyst Student",
                "Work with our product team to analyze user behavior data and build reports.",
                "SQL, Python, Pandas, data visualization tools",
                "Belgrade, Serbia",
                EmploymentType.STUDENT_WORK, WorkMode.REMOTE,
                LocalDate.of(2026, 9, 15));

        // ── Applications ──────────────────────────────────────────────────────
        seedApplication(backendJob,  user1, cv1, ApplicationStatus.APPLIED);
        seedApplication(frontendJob, user2, cv2, ApplicationStatus.SHORTLISTED);
        seedApplication(aiJob,       user4, cv4, ApplicationStatus.APPLIED);
        seedApplication(fullstackJob,user3, cv3, ApplicationStatus.SHORTLISTED);
        seedApplication(dataJob,     user4, cv4, ApplicationStatus.APPLIED);
        seedApplication(devopsJob,   user1, cv1, ApplicationStatus.APPLIED);
        seedApplication(backendJob,  user3, cv3, ApplicationStatus.SHORTLISTED);

        // ── Summary ───────────────────────────────────────────────────────────
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Database seeding complete");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  admin@cvapp.com       / password  (ADMIN)");
        log.info("  company@cvapp.com     / password  (BEST Nis)");
        log.info("  company2@cvapp.com    / password  (Google)");
        log.info("  company3@cvapp.com    / password  (Infostud)");
        log.info("  user@cvapp.com        / password  (Marko)");
        log.info("  user2@cvapp.com       / password  (Ana)");
        log.info("  user3@cvapp.com       / password  (Stefan)");
        log.info("  user4@cvapp.com       / password  (Milica)");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ── Seed helpers ──────────────────────────────────────────────────────────

    private User seedUser(String email, String password, Role role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User saved = userRepository.save(User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(password))
                            .role(role)
                            .provider(AuthProvider.LOCAL)
                            .enabled(true)
                            .build());
                    log.info("Seeded user {} [{}]", email, role);
                    return saved;
                });
    }

    private Company seedCompany(User user, String name, String description,
                                String website, String industry) {
        return companyRepository.findByUser(user)
                .orElseGet(() -> {
                    Company saved = companyRepository.save(Company.builder()
                            .user(user)
                            .name(name)
                            .description(description)
                            .website(website)
                            .industry(industry)
                            .build());
                    log.info("Seeded company {}", name);
                    return saved;
                });
    }

    private CV seedCV(User user, String firstName, String lastName,
                      String phone, String address, String summary,
                      String linkedinUrl, String githubUrl,
                      List<SkillSeed> skills,
                      List<EducationSeed> educationList,
                      List<ExperienceSeed> experienceList) {
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

                    skills.forEach(s -> cv.getSkills().add(
                            Skill.builder().cv(cv).name(s.name()).level(s.level()).build()));

                    educationList.forEach(e -> cv.getEducation().add(
                            Education.builder().cv(cv)
                                    .institution(e.institution()).degree(e.degree())
                                    .fieldOfStudy(e.fieldOfStudy()).startDate(e.startDate())
                                    .endDate(e.endDate()).current(e.current()).build()));

                    experienceList.forEach(e -> cv.getExperience().add(
                            Experience.builder().cv(cv)
                                    .companyName(e.companyName()).position(e.position())
                                    .description(e.description()).startDate(e.startDate())
                                    .endDate(e.endDate()).current(e.current()).build()));

                    CV saved = cvRepository.save(cv);
                    log.info("Seeded CV for {} {}", firstName, lastName);
                    return saved;
                });
    }

    private Job seedJob(Company company, String title, String description,
                        String requirements, String location,
                        EmploymentType employmentType, WorkMode workMode,
                        LocalDate deadline) {
        return jobRepository.findByCompany(company, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(j -> j.getTitle().equals(title))
                .findFirst()
                .orElseGet(() -> {
                    Job saved = jobRepository.save(Job.builder()
                            .company(company).title(title).description(description)
                            .requirements(requirements).location(location)
                            .employmentType(employmentType).workMode(workMode)
                            .deadline(deadline).active(true).build());
                    log.info("Seeded job {}", title);
                    return saved;
                });
    }

    private void seedApplication(Job job, User user, CV cv, ApplicationStatus status) {
        if (jobApplicationRepository.existsByJobAndUser(job, user)) {
            log.info("Skipping application — {} already applied to {}", user.getEmail(), job.getTitle());
            return;
        }
        jobApplicationRepository.save(JobApplication.builder()
                .job(job).user(user).cv(cv).status(status).build());
        log.info("Seeded application: {} → {}", user.getEmail(), job.getTitle());
    }

    // ── Seed value objects ────────────────────────────────────────────────────

    private record SkillSeed(String name, String level) {}
    private record EducationSeed(String institution, String degree, String fieldOfStudy,
                                 LocalDate startDate, LocalDate endDate, boolean current) {}
    private record ExperienceSeed(String companyName, String position, String description,
                                  LocalDate startDate, LocalDate endDate, boolean current) {}
}