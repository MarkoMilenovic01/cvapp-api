package com.best.cvapp.cv.profile;

import com.best.cvapp.cv.education.Education;
import com.best.cvapp.cv.experience.Experience;
import com.best.cvapp.cv.project.Project;
import com.best.cvapp.cv.skill.Skill;
import com.best.cvapp.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cvs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @OneToMany(
            mappedBy = "cv",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @OrderBy("current DESC, startDate DESC")
    private List<Education> education = new ArrayList<>();

    @OneToMany(
            mappedBy = "cv",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @OrderBy("current DESC, startDate DESC")
    private List<Experience> experience = new ArrayList<>();

    @OneToMany(
            mappedBy = "cv",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @OrderBy("current DESC, startDate DESC")
    private List<Project> projects = new ArrayList<>();

    @OneToMany(
            mappedBy = "cv",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(name = "profile_photo_id", length = 300)
    private String profilePhotoId;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "pdf_public_id", length = 300)
    private String pdfPublicId;
}
