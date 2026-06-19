package com.best.cvapp.company;

import com.best.cvapp.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;
    private String website;
    private String industry;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ── File upload ───────────────────────────────────────────────────────────

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "photo_public_id")
    private String photoPublicId;


}