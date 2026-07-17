package com.best.cvapp.cv.skill;

import com.best.cvapp.cv.profile.CV;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "skills",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_skills_cv_name",
                columnNames = {"cv_id", "name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_id", nullable = false)
    private CV cv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private SkillName name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillLevel level;
}
