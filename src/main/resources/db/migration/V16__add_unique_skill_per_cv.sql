ALTER TABLE skills
    ADD CONSTRAINT uk_skills_cv_name UNIQUE (cv_id, name);
