package com.best.cvapp.cv.project;

import com.best.cvapp.cv.project.dto.ProjectRequest;
import com.best.cvapp.cv.project.dto.ProjectResponse;
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
@RequestMapping("/api/user/cv/projects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAll(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(projectService.getAll(currentUser));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> add(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ProjectResponse response =
                projectService.add(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(
                projectService.update(id, request, currentUser)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        projectService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}