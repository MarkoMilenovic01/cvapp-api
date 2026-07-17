package com.best.cvapp.shared.seed;

import com.best.cvapp.company.profile.Company;
import com.best.cvapp.company.profile.CompanyRepository;
import com.best.cvapp.cv.education.Education;
import com.best.cvapp.cv.experience.Experience;
import com.best.cvapp.cv.experience.ExperienceType;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.project.Project;
import com.best.cvapp.cv.project.ProjectRepository;
import com.best.cvapp.cv.skill.Skill;
import com.best.cvapp.auth.oauth.AuthProvider;
import com.best.cvapp.cv.skill.SkillLevel;
import com.best.cvapp.cv.skill.SkillName;
import com.best.cvapp.job.application.JobApplication;
import com.best.cvapp.job.application.JobApplicationRepository;
import com.best.cvapp.job.core.*;
import com.best.cvapp.user.Role;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CVRepository cvRepository;
    private final ProjectRepository projectRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ── Users ─────────────────────────────────────────────────────────────
        seedUser("admin@cvapp.com", "Password123!", Role.ADMIN);
        User companyU1  = seedUser("company@cvapp.com",  "Password123!", Role.COMPANY);
        User companyU2  = seedUser("company2@cvapp.com", "Password123!", Role.COMPANY);
        User companyU3  = seedUser("company3@cvapp.com", "Password123!", Role.COMPANY);
        User user1      = seedUser("user@cvapp.com",     "Password123!", Role.USER);
        User user2      = seedUser("user2@cvapp.com",    "Password123!", Role.USER);
        User user3      = seedUser("user3@cvapp.com",    "Password123!", Role.USER);
        User user4      = seedUser("user4@cvapp.com",    "Password123!", Role.USER);

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
                        new SkillSeed(SkillName.JAVA, SkillLevel.ADVANCED),
                        new SkillSeed(SkillName.SPRING_BOOT, SkillLevel.INTERMEDIATE),
                        new SkillSeed(SkillName.POSTGRESQL, SkillLevel.INTERMEDIATE),
                        new SkillSeed(SkillName.DOCKER, SkillLevel.BEGINNER),
                        new SkillSeed(SkillName.GIT, SkillLevel.ADVANCED)
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
                                ExperienceType.INTERNSHIP,
                                "Built REST APIs using Spring Boot, managed PostgreSQL schemas and wrote unit tests.",
                                LocalDate.of(2023, 6, 1),
                                LocalDate.of(2023, 9, 30),
                                false)
                ),
                List.of(
                        new ProjectSeed(
                                "CV Application Platform",
                                "Full-stack job and CV platform built with Spring Boot, React, PostgreSQL and Docker.",
                                null,
                                "https://github.com/marko-milenovic/cvapp",
                                LocalDate.of(2026, 1, 10),
                                null,
                                true)
                )
        );

        CV cv2 = seedCV(
                user2, "Ana", "Petrovic", "0697654321", "Ljubljana, Slovenia",
                "Frontend developer student focused on React, TypeScript and accessible UI design.",
                "https://linkedin.com/in/ana-petrovic",
                "https://github.com/ana-petrovic",
                List.of(
                        new SkillSeed(SkillName.REACT, SkillLevel.ADVANCED),
                        new SkillSeed(SkillName.TYPESCRIPT, SkillLevel.INTERMEDIATE),
                        new SkillSeed(SkillName.CSS, SkillLevel.ADVANCED),
                        new SkillSeed(SkillName.FIGMA, SkillLevel.INTERMEDIATE),
                        new SkillSeed(SkillName.GIT, SkillLevel.INTERMEDIATE)
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
                                ExperienceType.INTERNSHIP,
                                "Developed responsive landing pages and component libraries in React.",
                                LocalDate.of(2024, 2, 1),
                                LocalDate.of(2024, 5, 31),
                                false)
                ),
                List.of(
                        new ProjectSeed(
                                "Accessible UI Component Library",
                                "Reusable and accessible React components documented for student projects.",
                                null,
                                "https://github.com/ana-petrovic/ui-components",
                                LocalDate.of(2024, 6, 1),
                                LocalDate.of(2024, 9, 30),
                                false)
                )
        );

        CV cv3 = seedCV(
                user3, "Stefan", "Jovanovic", "0621112233", "Belgrade, Serbia",
                "Full-stack developer with experience in React and Node.js, currently exploring cloud-native development.",
                "https://linkedin.com/in/stefan-jovanovic",
                "https://github.com/stefan-jovanovic",
                List.of(
                        new SkillSeed(SkillName.REACT, SkillLevel.ADVANCED),
                        new SkillSeed(SkillName.NODE_JS, SkillLevel.ADVANCED),
                        new SkillSeed(SkillName.MONGODB, SkillLevel.INTERMEDIATE),
                        new SkillSeed(SkillName.AWS, SkillLevel.BEGINNER),
                        new SkillSeed(SkillName.GIT, SkillLevel.ADVANCED)
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
                                ExperienceType.FULL_TIME,
                                "Worked on full-stack features for enterprise clients using React and Node.js microservices.",
                                LocalDate.of(2023, 9, 1),
                                null,
                                true)
                ),
                List.of(
                        new ProjectSeed(
                                "Cloud Task Manager",
                                "Full-stack task management application built with React, Node.js and MongoDB.",
                                null,
                                "https://github.com/stefan-jovanovic/cloud-task-manager",
                                LocalDate.of(2024, 7, 1),
                                LocalDate.of(2024, 12, 15),
                                false)
                )
        );

        CV cv4 = seedCV(
                user4, "Milica", "Nikolic", "0638887766", "Novi Sad, Serbia",
                "Data science student with a strong interest in machine learning, Python and data visualization.",
                "https://linkedin.com/in/milica-nikolic",
                "https://github.com/milica-nikolic",
                List.of(
                        new SkillSeed(SkillName.PYTHON, SkillLevel.ADVANCED),
                        new SkillSeed(SkillName.MACHINE_LEARNING, SkillLevel.INTERMEDIATE),
                        new SkillSeed(SkillName.PANDAS, SkillLevel.ADVANCED),
                        new SkillSeed(SkillName.TENSORFLOW, SkillLevel.BEGINNER),
                        new SkillSeed(SkillName.SQL, SkillLevel.INTERMEDIATE)
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
                                ExperienceType.INTERNSHIP,
                                "Assisted in building and evaluating classification models for NLP tasks using Python and TensorFlow.",
                                LocalDate.of(2024, 3, 1),
                                LocalDate.of(2024, 6, 30),
                                false)
                ),
                List.of(
                        new ProjectSeed(
                                "Machine Learning Classification Dashboard",
                                "Dashboard for training, comparing and visualizing classification models.",
                                null,
                                "https://github.com/milica-nikolic/ml-dashboard",
                                LocalDate.of(2024, 8, 1),
                                null,
                                true)
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
                LocalDate.of(2026, 8, 15));

        Job dataJob = seedJob(infostud,
                "Data Analyst Student",
                "Work with our product team to analyze user behavior data and build reports.",
                "SQL, Python, Pandas, data visualization tools",
                "Belgrade, Serbia",
                EmploymentType.STUDENT_WORK, WorkMode.REMOTE,
                LocalDate.of(2026, 9, 15));

        // ── Applications ──────────────────────────────────────────────────────
        seedApplication(backendJob, user1, cv1, ApplicationStatus.APPLIED);
        seedApplication(frontendJob, user2, cv2, ApplicationStatus.SHORTLISTED);
        seedApplication(aiJob, user4, cv4, ApplicationStatus.APPLIED);
        seedApplication(fullstackJob, user3, cv3, ApplicationStatus.SHORTLISTED);
        seedApplication(dataJob, user4, cv4, ApplicationStatus.APPLIED);
        seedApplication(devopsJob, user1, cv1, ApplicationStatus.APPLIED);
        seedApplication(backendJob, user3, cv3, ApplicationStatus.SHORTLISTED);

        // ── Summary ───────────────────────────────────────────────────────────
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Database seeding complete");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Default password for new seed accounts: Password123!");
        log.info("  admin@cvapp.com       (ADMIN)");
        log.info("  company@cvapp.com     (BEST Nis)");
        log.info("  company2@cvapp.com    (Google)");
        log.info("  company3@cvapp.com    (Infostud)");
        log.info("  user@cvapp.com        (Marko)");
        log.info("  user2@cvapp.com       (Ana)");
        log.info("  user3@cvapp.com       (Stefan)");
        log.info("  user4@cvapp.com       (Milica)");
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
                      List<ExperienceSeed> experienceList,
                      List<ProjectSeed> projectList) {
        CV cv = cvRepository.findByUser(user)
                .orElseGet(() -> {
                    CV newCV = CV.builder()
                            .user(user)
                            .firstName(firstName)
                            .lastName(lastName)
                            .phone(phone)
                            .address(address)
                            .summary(summary)
                            .linkedinUrl(linkedinUrl)
                            .githubUrl(githubUrl)
                            .build();

                    skills.forEach(s -> newCV.getSkills().add(
                            Skill.builder().cv(newCV).name(s.name()).level(s.level()).build()));

                    educationList.forEach(e -> newCV.getEducation().add(
                            Education.builder().cv(newCV)
                                    .institution(e.institution()).degree(e.degree())
                                    .fieldOfStudy(e.fieldOfStudy()).startDate(e.startDate())
                                    .endDate(e.endDate()).current(e.current()).build()));

                    experienceList.forEach(e -> newCV.getExperience().add(
                            Experience.builder()
                                    .cv(newCV)
                                    .companyName(e.companyName())
                                    .position(e.position())
                                    .experienceType(e.experienceType())
                                    .description(e.description())
                                    .startDate(e.startDate())
                                    .endDate(e.endDate())
                                    .current(e.current())
                                    .build()
                    ));

                    CV saved = cvRepository.save(newCV);
                    log.info("Seeded CV for {} {}", firstName, lastName);
                    return saved;
                });

        seedProjects(cv, projectList);
        return cv;
    }

    private void seedProjects(CV cv, List<ProjectSeed> projectList) {
        List<Project> existingProjects = projectRepository.findByCv(cv);

        projectList.stream()
                .filter(seed -> existingProjects.stream()
                        .noneMatch(project -> project.getName().equals(seed.name())))
                .forEach(seed -> {
                    projectRepository.save(Project.builder()
                            .cv(cv)
                            .name(seed.name())
                            .description(seed.description())
                            .projectUrl(seed.projectUrl())
                            .repositoryUrl(seed.repositoryUrl())
                            .startDate(seed.startDate())
                            .endDate(seed.endDate())
                            .current(seed.current())
                            .build());
                    log.info("Seeded project {}", seed.name());
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

    private record SkillSeed(SkillName name, SkillLevel level) {}
    private record EducationSeed(String institution, String degree, String fieldOfStudy,
                                 LocalDate startDate, LocalDate endDate, boolean current) {}
    private record ExperienceSeed(
            String companyName,
            String position,
            ExperienceType experienceType,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            boolean current
    ) {}
    private record ProjectSeed(
            String name,
            String description,
            String projectUrl,
            String repositoryUrl,
            LocalDate startDate,
            LocalDate endDate,
            boolean current
    ) {}
}
