package com.best.cvapp.cv.profile.dto;

import com.best.cvapp.cv.education.dto.EducationResponse;
import com.best.cvapp.cv.experience.dto.ExperienceResponse;
import com.best.cvapp.cv.skill.dto.SkillResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CVResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String summary;
    private String linkedinUrl;
    private String githubUrl;
    private List<EducationResponse> education;
    private List<ExperienceResponse> experience;
    private List<SkillResponse> skills;
    private LocalDateTime createdAt;

    private String profilePhotoUrl;
    private String pdfUrl;
}