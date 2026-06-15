package com.best.cvapp.cv.education;

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
public class EducationService {

    private final EducationRepository educationRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;

    public List<EducationResponse> getAll() {
        CV cv = getAuthenticatedUserCV();
        return educationRepository.findByCv(cv).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EducationResponse add(EducationRequest request) {
        CV cv = getAuthenticatedUserCV();

        Education education = Education.builder()
                .cv(cv)
                .institution(request.getInstitution())
                .degree(request.getDegree())
                .fieldOfStudy(request.getFieldOfStudy())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .current(request.isCurrent())
                .build();

        return mapToResponse(educationRepository.save(education));
    }

    public EducationResponse update(Long id, EducationRequest request) {
        Education education = getEducationAndVerifyOwnership(id);

        education.setInstitution(request.getInstitution());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setCurrent(request.isCurrent());

        return mapToResponse(educationRepository.save(education));
    }

    public void delete(Long id) {
        Education education = getEducationAndVerifyOwnership(id);
        educationRepository.delete(education);
    }

    private Education getEducationAndVerifyOwnership(Long id) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Education not found"));

        CV cv = getAuthenticatedUserCV();

        if (!education.getCv().getId().equals(cv.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return education;
    }

    private CV getAuthenticatedUserCV() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found — create your CV first"));
    }

    private EducationResponse mapToResponse(Education e) {
        return new EducationResponse(
                e.getId(),
                e.getInstitution(),
                e.getDegree(),
                e.getFieldOfStudy(),
                e.getStartDate(),
                e.getEndDate(),
                e.isCurrent()
        );
    }
}