package com.best.cvapp.cv.skill;

import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.profile.exception.CVNotFoundException;
import com.best.cvapp.cv.skill.dto.SkillRequest;
import com.best.cvapp.cv.skill.dto.SkillResponse;
import com.best.cvapp.cv.skill.exception.DuplicateSkillException;
import com.best.cvapp.cv.skill.exception.SkillNotFoundException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles skills for the authenticated user's CV.
 *
 * Flow:
 * 1. Load the CV belonging to the authenticated user.
 * 2. List its skills.
 * 3. Create or update a skill from the submitted name and level.
 * 4. Verify that a skill belongs to the user's CV before changing it.
 * 5. Return the saved skill or delete it.
 */
@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final CVRepository cvRepository;

    @Transactional(readOnly = true)
    public List<SkillResponse> getAll(User currentUser) {
        CV cv = getUserCV(currentUser);

        return skillRepository.findByCv(cv).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public SkillResponse add(
            SkillRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        if (skillRepository.existsByCvAndName(cv, request.name())) {
            throw new DuplicateSkillException();
        }

        Skill skill = Skill.builder()
                .cv(cv)
                .name(request.name())
                .level(request.level())
                .build();

        return save(skill);
    }

    @Transactional
    public SkillResponse update(
            Long id,
            SkillRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        Skill skill = skillRepository.findByIdAndCv(id, cv)
                .orElseThrow(SkillNotFoundException::new);

        if (skillRepository.existsByCvAndNameAndIdNot(cv, request.name(), id)) {
            throw new DuplicateSkillException();
        }

        skill.setName(request.name());
        skill.setLevel(request.level());

        return save(skill);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        CV cv = getUserCV(currentUser);

        Skill skill = skillRepository.findByIdAndCv(id, cv)
                .orElseThrow(SkillNotFoundException::new);

        skillRepository.delete(skill);
    }

    private CV getUserCV(User currentUser) {
        return cvRepository.findByUser(currentUser)
                .orElseThrow(CVNotFoundException::new);
    }

    private SkillResponse save(Skill skill) {
        try {
            return mapToResponse(skillRepository.saveAndFlush(skill));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateSkillException();
        }
    }

    private SkillResponse mapToResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getLevel()
        );
    }
}
