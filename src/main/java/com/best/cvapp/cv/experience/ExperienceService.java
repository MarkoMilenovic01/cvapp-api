package com.best.cvapp.cv.experience;

import com.best.cvapp.cv.experience.dto.ExperienceRequest;
import com.best.cvapp.cv.experience.dto.ExperienceResponse;
import com.best.cvapp.cv.experience.exception.ExperienceNotFoundException;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.profile.exception.CVNotFoundException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles experience entries for the authenticated user's CV.
 *
 * Flow:
 * 1. Load the CV belonging to the authenticated user.
 * 2. List its experience entries in current and start-date order.
 * 3. Create or update an experience entry from the submitted details.
 * 4. Verify that an entry belongs to the user's CV before changing it.
 * 5. Return the saved entry or delete it.
 */
@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final CVRepository cvRepository;

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getAll(User currentUser) {
        CV cv = getUserCV(currentUser);

        return experienceRepository.findByCvOrderByCurrentDescStartDateDesc(cv).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ExperienceResponse add(
            ExperienceRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        Experience experience = Experience.builder()
                .cv(cv)
                .companyName(request.companyName())
                .position(request.position())
                .experienceType(request.experienceType())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .current(request.current())
                .build();

        return mapToResponse(experienceRepository.save(experience));
    }

    @Transactional
    public ExperienceResponse update(
            Long id,
            ExperienceRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        Experience experience = experienceRepository.findByIdAndCv(id, cv)
                .orElseThrow(ExperienceNotFoundException::new);

        experience.setCompanyName(request.companyName());
        experience.setPosition(request.position());
        experience.setExperienceType(request.experienceType());
        experience.setDescription(request.description());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setCurrent(request.current());

        return mapToResponse(experienceRepository.save(experience));
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        CV cv = getUserCV(currentUser);

        Experience experience = experienceRepository.findByIdAndCv(id, cv)
                .orElseThrow(ExperienceNotFoundException::new);

        experienceRepository.delete(experience);
    }

    private CV getUserCV(User currentUser) {
        return cvRepository.findByUser(currentUser)
                .orElseThrow(CVNotFoundException::new);
    }

    private ExperienceResponse mapToResponse(Experience experience) {
        return new ExperienceResponse(
                experience.getId(),
                experience.getCompanyName(),
                experience.getPosition(),
                experience.getExperienceType(),
                experience.getDescription(),
                experience.getStartDate(),
                experience.getEndDate(),
                experience.isCurrent()
        );
    }
}
