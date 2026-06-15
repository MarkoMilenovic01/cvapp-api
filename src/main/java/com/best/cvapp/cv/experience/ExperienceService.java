package com.best.cvapp.cv.experience;

import com.best.cvapp.cv.CV;
import com.best.cvapp.cv.CVRepository;
import com.best.cvapp.user.User;
import com.best.cvapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;

    public List<ExperienceResponse> getAll() {
        CV cv = getAuthenticatedUserCV();
        return experienceRepository.findByCv(cv).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ExperienceResponse add(ExperienceRequest request) {
        CV cv = getAuthenticatedUserCV();

        Experience experience = Experience.builder()
                .cv(cv)
                .companyName(request.getCompanyName())
                .position(request.getPosition())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .current(request.isCurrent())
                .build();

        return mapToResponse(experienceRepository.save(experience));
    }

    public ExperienceResponse update(Long id, ExperienceRequest request) {
        Experience experience = getExperienceAndVerifyOwnership(id);

        experience.setCompanyName(request.getCompanyName());
        experience.setPosition(request.getPosition());
        experience.setDescription(request.getDescription());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setCurrent(request.isCurrent());

        return mapToResponse(experienceRepository.save(experience));
    }

    public void delete(Long id) {
        Experience experience = getExperienceAndVerifyOwnership(id);
        experienceRepository.delete(experience);
    }

    private Experience getExperienceAndVerifyOwnership(Long id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Experience not found"));

        CV cv = getAuthenticatedUserCV();

        if (!experience.getCv().getId().equals(cv.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return experience;
    }

    private CV getAuthenticatedUserCV() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found — create your CV first"));
    }

    private ExperienceResponse mapToResponse(Experience e) {
        return new ExperienceResponse(
                e.getId(),
                e.getCompanyName(),
                e.getPosition(),
                e.getDescription(),
                e.getStartDate(),
                e.getEndDate(),
                e.isCurrent()
        );
    }
}