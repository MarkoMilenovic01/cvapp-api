package com.best.cvapp.cv.education;

import com.best.cvapp.cv.education.dto.EducationRequest;
import com.best.cvapp.cv.education.dto.EducationResponse;
import com.best.cvapp.cv.education.exception.EducationNotFoundException;
import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.profile.exception.CVNotFoundException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles education entries for the authenticated user's CV.
 *
 * Flow:
 * 1. Load the CV belonging to the authenticated user.
 * 2. List its education entries in current and start-date order.
 * 3. Create or update an education entry from the submitted details.
 * 4. Verify that an entry belongs to the user's CV before changing it.
 * 5. Return the saved entry or delete it.
 */
@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final CVRepository cvRepository;

    @Transactional(readOnly = true)
    public List<EducationResponse> getAll(User currentUser) {
        CV cv = getUserCV(currentUser);

        return educationRepository.findByCvOrderByCurrentDescStartDateDesc(cv).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public EducationResponse add(
            EducationRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        Education education = Education.builder()
                .cv(cv)
                .institution(request.institution())
                .degree(request.degree())
                .fieldOfStudy(request.fieldOfStudy())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .current(request.current())
                .build();

        return mapToResponse(educationRepository.save(education));
    }

    @Transactional
    public EducationResponse update(
            Long id,
            EducationRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        Education education = educationRepository.findByIdAndCv(id, cv)
                .orElseThrow(EducationNotFoundException::new);

        education.setInstitution(request.institution());
        education.setDegree(request.degree());
        education.setFieldOfStudy(request.fieldOfStudy());
        education.setStartDate(request.startDate());
        education.setEndDate(request.endDate());
        education.setCurrent(request.current());

        return mapToResponse(educationRepository.save(education));
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        CV cv = getUserCV(currentUser);

        Education education = educationRepository.findByIdAndCv(id, cv)
                .orElseThrow(EducationNotFoundException::new);

        educationRepository.delete(education);
    }

    private CV getUserCV(User currentUser) {
        return cvRepository.findByUser(currentUser)
                .orElseThrow(CVNotFoundException::new);
    }

    private EducationResponse mapToResponse(Education education) {
        return new EducationResponse(
                education.getId(),
                education.getInstitution(),
                education.getDegree(),
                education.getFieldOfStudy(),
                education.getStartDate(),
                education.getEndDate(),
                education.isCurrent()
        );
    }
}
