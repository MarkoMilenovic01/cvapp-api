package com.best.cvapp.cv.skill;

import com.best.cvapp.cv.profile.CV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByCv(CV cv);

    Optional<Skill> findByIdAndCv(Long id, CV cv);

    boolean existsByCvAndName(CV cv, SkillName name);

    boolean existsByCvAndNameAndIdNot(CV cv, SkillName name, Long id);
}
