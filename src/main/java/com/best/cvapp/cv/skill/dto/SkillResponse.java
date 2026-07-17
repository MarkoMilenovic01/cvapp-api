package com.best.cvapp.cv.skill.dto;

import com.best.cvapp.cv.skill.SkillLevel;
import com.best.cvapp.cv.skill.SkillName;

public record SkillResponse(
        Long id,
        SkillName name,
        SkillLevel level
) {
}