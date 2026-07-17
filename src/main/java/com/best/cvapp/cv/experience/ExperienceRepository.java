package com.best.cvapp.cv.experience;

import com.best.cvapp.cv.profile.CV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByCvOrderByCurrentDescStartDateDesc(CV cv);

    Optional<Experience> findByIdAndCv(Long id, CV cv);
}
