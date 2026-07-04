package com.best.cvapp.cv.experience;

import com.best.cvapp.cv.experience.dto.ExperienceRequest;
import com.best.cvapp.cv.experience.dto.ExperienceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cv/experience")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> getAll() {
        return ResponseEntity.ok(experienceService.getAll());
    }

    @PostMapping
    public ResponseEntity<ExperienceResponse> add(@RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(experienceService.add(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperienceResponse> update(@PathVariable Long id, @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(experienceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        experienceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}