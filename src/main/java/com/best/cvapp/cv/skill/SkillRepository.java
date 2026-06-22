package com.best.cvapp.cv.skill;

import com.best.cvapp.cv.profile.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByCv(CV cv);
}