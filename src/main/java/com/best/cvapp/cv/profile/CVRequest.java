package com.best.cvapp.cv.profile;

import com.best.cvapp.cv.education.EducationRequest;
import com.best.cvapp.cv.experience.ExperienceRequest;
import com.best.cvapp.cv.skill.SkillRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CVRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String summary;
    private String linkedinUrl;
    private String githubUrl;
    private List<EducationRequest> education;
    private List<ExperienceRequest> experience;
    private List<SkillRequest> skills;
}