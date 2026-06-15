package com.best.cvapp.cv;

import com.best.cvapp.cv.education.Education;
import com.best.cvapp.cv.education.EducationResponse;
import com.best.cvapp.cv.experience.Experience;
import com.best.cvapp.cv.experience.ExperienceResponse;
import com.best.cvapp.cv.skill.Skill;
import com.best.cvapp.cv.skill.SkillResponse;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CVService {

    private final CVRepository cvRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public CVResponse getMyCV() {
        User user = getAuthenticatedUser();
        CV cv = cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
        return mapToResponse(cv);
    }

    @Transactional
    public CVResponse createOrUpdateCV(CVRequest request) {
        User user = getAuthenticatedUser();

        CV cv = cvRepository.findByUser(user)
                .orElse(CV.builder().user(user).build());

        cv.setFirstName(request.getFirstName());
        cv.setLastName(request.getLastName());
        cv.setPhone(request.getPhone());
        cv.setAddress(request.getAddress());
        cv.setSummary(request.getSummary());
        cv.setLinkedinUrl(request.getLinkedinUrl());
        cv.setGithubUrl(request.getGithubUrl());

        // Update education
        cv.getEducation().clear();
        if (request.getEducation() != null) {
            request.getEducation().forEach(e -> cv.getEducation().add(
                    Education.builder()
                            .cv(cv)
                            .institution(e.getInstitution())
                            .degree(e.getDegree())
                            .fieldOfStudy(e.getFieldOfStudy())
                            .startDate(e.getStartDate())
                            .endDate(e.getEndDate())
                            .current(e.isCurrent())
                            .build()
            ));
        }

        // Update experience
        cv.getExperience().clear();
        if (request.getExperience() != null) {
            request.getExperience().forEach(e -> cv.getExperience().add(
                    Experience.builder()
                            .cv(cv)
                            .companyName(e.getCompanyName())
                            .position(e.getPosition())
                            .description(e.getDescription())
                            .startDate(e.getStartDate())
                            .endDate(e.getEndDate())
                            .current(e.isCurrent())
                            .build()
            ));
        }

        // Update skills
        cv.getSkills().clear();
        if (request.getSkills() != null) {
            request.getSkills().forEach(s -> cv.getSkills().add(
                    Skill.builder()
                            .cv(cv)
                            .name(s.getName())
                            .level(s.getLevel())
                            .build()
            ));
        }

        cvRepository.save(cv);
        return mapToResponse(cv);
    }

    @Transactional
    public void deleteCV() {
        User user = getAuthenticatedUser();
        CV cv = cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
        cvRepository.delete(cv);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private CVResponse mapToResponse(CV cv) {
        List<EducationResponse> education = cv.getEducation().stream()
                .map(e -> new EducationResponse(
                        e.getId(),
                        e.getInstitution(),
                        e.getDegree(),
                        e.getFieldOfStudy(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.isCurrent()
                )).toList();

        List<ExperienceResponse> experience = cv.getExperience().stream()
                .map(e -> new ExperienceResponse(
                        e.getId(),
                        e.getCompanyName(),
                        e.getPosition(),
                        e.getDescription(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.isCurrent()
                )).toList();

        List<SkillResponse> skills = cv.getSkills().stream()
                .map(s -> new SkillResponse(
                        s.getId(),
                        s.getName(),
                        s.getLevel()
                )).toList();

        return new CVResponse(
                cv.getId(),
                cv.getFirstName(),
                cv.getLastName(),
                cv.getPhone(),
                cv.getAddress(),
                cv.getSummary(),
                cv.getLinkedinUrl(),
                cv.getGithubUrl(),
                education,
                experience,
                skills,
                cv.getCreatedAt()
        );
    }
}