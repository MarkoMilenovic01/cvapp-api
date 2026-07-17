package com.best.cvapp.cv.experience;

import com.best.cvapp.cv.experience.dto.ExperienceRequest;
import com.best.cvapp.cv.experience.dto.ExperienceResponse;
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
@RequestMapping("/api/user/cv/experience")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> getAll(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(experienceService.getAll(currentUser));
    }

    @PostMapping
    public ResponseEntity<ExperienceResponse> add(
            @Valid @RequestBody ExperienceRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ExperienceResponse response =
                experienceService.add(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                experienceService.update(id, request, currentUser)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        experienceService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}