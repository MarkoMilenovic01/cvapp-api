package com.best.cvapp.company.history;

import com.best.cvapp.company.Company;
import com.best.cvapp.cv.CV;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cv_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "cv_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "cv_id", nullable = false)
    private CV cv;

    @Column(nullable = false)
    private LocalDateTime viewedAt;
}