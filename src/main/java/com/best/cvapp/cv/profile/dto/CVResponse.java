package com.best.cvapp.cv.profile.dto;

import com.best.cvapp.cv.education.dto.EducationResponse;
import com.best.cvapp.cv.experience.dto.ExperienceResponse;
import com.best.cvapp.cv.project.dto.ProjectResponse;
import com.best.cvapp.cv.skill.dto.SkillResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CVResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String address,
        String summary,
        String linkedinUrl,
        String githubUrl,
        List<EducationResponse> education,
        List<ExperienceResponse> experience,
        List<ProjectResponse> projects,
        List<SkillResponse> skills,
        LocalDateTime createdAt,
        String profilePhotoUrl,
        String pdfUrl
) {
}