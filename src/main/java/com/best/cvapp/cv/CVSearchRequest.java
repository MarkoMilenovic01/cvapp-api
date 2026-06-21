package com.best.cvapp.cv;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CVSearchRequest {

    // Full-text search across name, summary, skills, experience, education
    private String keyword;

    // Filter by specific skill name e.g. "Java", "React"
    private String skill;

    // Filter by location in address field
    private String location;
}