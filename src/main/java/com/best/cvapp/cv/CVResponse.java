package com.best.cvapp.cv;

import com.best.cvapp.cv.education.EducationResponse;
import com.best.cvapp.cv.experience.ExperienceResponse;
import com.best.cvapp.cv.skill.SkillResponse;
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
}