package com.best.cvapp.cv.project;

import com.best.cvapp.cv.profile.CV;
import com.best.cvapp.cv.profile.CVRepository;
import com.best.cvapp.cv.profile.exception.CVNotFoundException;
import com.best.cvapp.cv.project.dto.ProjectRequest;
import com.best.cvapp.cv.project.dto.ProjectResponse;
import com.best.cvapp.cv.project.exception.ProjectNotFoundException;
import com.best.cvapp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles projects for the authenticated user's CV.
 *
 * Flow:
 * 1. Load the CV belonging to the authenticated user.
 * 2. List its projects in current and start-date order.
 * 3. Create or update a project from the submitted details.
 * 4. Verify that a project belongs to the user's CV before changing it.
 * 5. Return the saved project or delete it.
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CVRepository cvRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll(User currentUser) {
        CV cv = getUserCV(currentUser);

        return projectRepository.findByCvOrderByCurrentDescStartDateDesc(cv).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse add(
            ProjectRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        Project project = Project.builder()
                .cv(cv)
                .name(request.name())
                .description(request.description())
                .projectUrl(request.projectUrl())
                .repositoryUrl(request.repositoryUrl())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .current(request.current())
                .build();

        return mapToResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse update(
            Long id,
            ProjectRequest request,
            User currentUser
    ) {
        CV cv = getUserCV(currentUser);

        Project project = projectRepository.findByIdAndCv(id, cv)
                .orElseThrow(ProjectNotFoundException::new);

        project.setName(request.name());
        project.setDescription(request.description());
        project.setProjectUrl(request.projectUrl());
        project.setRepositoryUrl(request.repositoryUrl());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setCurrent(request.current());

        return mapToResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        CV cv = getUserCV(currentUser);

        Project project = projectRepository.findByIdAndCv(id, cv)
                .orElseThrow(ProjectNotFoundException::new);

        projectRepository.delete(project);
    }

    private CV getUserCV(User currentUser) {
        return cvRepository.findByUser(currentUser)
                .orElseThrow(CVNotFoundException::new);
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getProjectUrl(),
                project.getRepositoryUrl(),
                project.getStartDate(),
                project.getEndDate(),
                project.isCurrent()
        );
    }
}
