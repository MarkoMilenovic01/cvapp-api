ALTER TABLE jobs
    ADD CONSTRAINT chk_jobs_description_length
        CHECK (char_length(description) <= 10000),
    ADD CONSTRAINT chk_jobs_requirements_length
        CHECK (requirements IS NULL OR char_length(requirements) <= 10000);
