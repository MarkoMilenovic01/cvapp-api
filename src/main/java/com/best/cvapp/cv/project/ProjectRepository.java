package com.best.cvapp.cv.project;

import com.best.cvapp.cv.profile.CV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCv(CV cv);

    List<Project> findByCvOrderByCurrentDescStartDateDesc(CV cv);

    Optional<Project> findByIdAndCv(Long id, CV cv);
}
