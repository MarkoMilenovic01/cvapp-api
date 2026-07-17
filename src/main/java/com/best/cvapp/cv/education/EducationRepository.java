package com.best.cvapp.cv.education;

import com.best.cvapp.cv.profile.CV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByCvOrderByCurrentDescStartDateDesc(CV cv);

    Optional<Education> findByIdAndCv(Long id, CV cv);
}
