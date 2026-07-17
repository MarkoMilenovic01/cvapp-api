package com.best.cvapp.cv.profile;

import com.best.cvapp.cv.education.dto.EducationResponse;
import com.best.cvapp.cv.experience.dto.ExperienceResponse;
import com.best.cvapp.cv.profile.dto.CVRequest;
import com.best.cvapp.cv.profile.dto.CVResponse;
import com.best.cvapp.cv.profile.exception.CVNotFoundException;
import com.best.cvapp.cv.project.dto.ProjectResponse;
import com.best.cvapp.cv.skill.dto.SkillResponse;
import com.best.cvapp.shared.storage.CloudinaryService;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles the authenticated user's CV profile.
 *
 * Flow:
 * 1. Load the CV belonging to the authenticated user.
 * 2. Create a CV when one does not exist or update its profile details.
 * 3. Include education, experience, skills, and projects in the response.
 * 4. Delete stored profile files when the CV is removed.
 * 5. Save or delete the CV.
 */
@Service
@RequiredArgsConstructor
public class CVService {

    private final CVRepository cvRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public CVResponse getMyCV(User currentUser) {
        CV cv = cvRepository.findByUser(currentUser)
                .orElseThrow(CVNotFoundException::new);

        return mapToResponse(cv);
    }

    @Transactional
    public CVResponse createOrUpdateCV(
            CVRequest request,
            User currentUser
    ) {
        CV cv = cvRepository.findByUser(currentUser)
                .orElseGet(() -> CV.builder()
                        .user(currentUser)
                        .build());

        cv.setFirstName(request.firstName());
        cv.setLastName(request.lastName());
        cv.setPhone(request.phone());
        cv.setAddress(request.address());
        cv.setSummary(request.summary());
        cv.setLinkedinUrl(request.linkedinUrl());
        cv.setGithubUrl(request.githubUrl());

        CV savedCV = cvRepository.save(cv);

        return mapToResponse(savedCV);
    }

    @Transactional
    public void deleteCV(User currentUser) {
        CV cv = cvRepository.findByUser(currentUser)
                .orElseThrow(CVNotFoundException::new);

        if (cv.getProfilePhotoId() != null) {
            cloudinaryService.deleteImage(cv.getProfilePhotoId());
        }

        if (cv.getPdfPublicId() != null) {
            cloudinaryService.deletePdf(cv.getPdfPublicId());
        }

        cvRepository.delete(cv);
    }

    public CVResponse mapToResponse(CV cv) {
        List<EducationResponse> education = cv.getEducation().stream()
                .map(entry -> new EducationResponse(
                        entry.getId(),
                        entry.getInstitution(),
                        entry.getDegree(),
                        entry.getFieldOfStudy(),
                        entry.getStartDate(),
                        entry.getEndDate(),
                        entry.isCurrent()
                ))
                .toList();

        List<ExperienceResponse> experience = cv.getExperience().stream()
                .map(entry -> new ExperienceResponse(
                        entry.getId(),
                        entry.getCompanyName(),
                        entry.getPosition(),
                        entry.getExperienceType(),
                        entry.getDescription(),
                        entry.getStartDate(),
                        entry.getEndDate(),
                        entry.isCurrent()
                ))
                .toList();

        List<SkillResponse> skills = cv.getSkills().stream()
                .map(entry -> new SkillResponse(
                        entry.getId(),
                        entry.getName(),
                        entry.getLevel()
                ))
                .toList();

        List<ProjectResponse> projects = cv.getProjects().stream()
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getProjectUrl(),
                        project.getRepositoryUrl(),
                        project.getStartDate(),
                        project.getEndDate(),
                        project.isCurrent()
                ))
                .toList();

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
                projects,
                skills,
                cv.getCreatedAt(),
                cv.getProfilePhotoUrl(),
                cv.getPdfUrl()
        );
    }
}
