CREATE TABLE projects (
                          id BIGSERIAL PRIMARY KEY,
                          cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          project_url VARCHAR(500),
                          repository_url VARCHAR(500),
                          start_date DATE,
                          end_date DATE,
                          current BOOLEAN NOT NULL DEFAULT FALSE
);