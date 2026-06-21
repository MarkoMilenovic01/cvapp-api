package com.best.cvapp.cv.skill;

import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
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
public class SkillService {

    private final SkillRepository skillRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;

    public List<SkillResponse> getAll() {
        CV cv = getAuthenticatedUserCV();
        return skillRepository.findByCv(cv).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SkillResponse add(SkillRequest request) {
        CV cv = getAuthenticatedUserCV();

        Skill skill = Skill.builder()
                .cv(cv)
                .name(request.getName())
                .level(request.getLevel())
                .build();

        return mapToResponse(skillRepository.save(skill));
    }

    public SkillResponse update(Long id, SkillRequest request) {
        Skill skill = getSkillAndVerifyOwnership(id);

        skill.setName(request.getName());
        skill.setLevel(request.getLevel());

        return mapToResponse(skillRepository.save(skill));
    }

    public void delete(Long id) {
        Skill skill = getSkillAndVerifyOwnership(id);
        skillRepository.delete(skill);
    }

    private Skill getSkillAndVerifyOwnership(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill not found"));

        CV cv = getAuthenticatedUserCV();

        if (!skill.getCv().getId().equals(cv.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return skill;
    }

    private CV getAuthenticatedUserCV() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return cvRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found — create your CV first"));
    }

    private SkillResponse mapToResponse(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.getLevel());
    }
}