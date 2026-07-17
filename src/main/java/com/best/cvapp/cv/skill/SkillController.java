package com.best.cvapp.cv.skill;

import com.best.cvapp.cv.skill.dto.SkillRequest;
import com.best.cvapp.cv.skill.dto.SkillResponse;
import com.best.cvapp.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cv/skills")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAll(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(skillService.getAll(currentUser));
    }

    @PostMapping
    public ResponseEntity<SkillResponse> add(
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        SkillResponse response = skillService.add(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                skillService.update(id, request, currentUser)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        skillService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}