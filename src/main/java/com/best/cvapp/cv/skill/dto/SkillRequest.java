package com.best.cvapp.cv.skill.dto;

import com.best.cvapp.cv.skill.SkillLevel;
import com.best.cvapp.cv.skill.SkillName;
import jakarta.validation.constraints.NotNull;

public record SkillRequest(

        @NotNull(message = "Skill name is required")
        SkillName name,

        @NotNull(message = "Skill level is required")
        SkillLevel level
) {
}