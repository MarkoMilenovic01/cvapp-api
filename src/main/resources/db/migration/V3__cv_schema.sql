CREATE TABLE cvs (
                     id BIGSERIAL PRIMARY KEY,
                     user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                     first_name VARCHAR(100),
                     last_name VARCHAR(100),
                     phone VARCHAR(20),
                     address VARCHAR(255),
                     summary TEXT,
                     linkedin_url VARCHAR(255),
                     github_url VARCHAR(255),
                     created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE education (
                           id BIGSERIAL PRIMARY KEY,
                           cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                           institution VARCHAR(255) NOT NULL,
                           degree VARCHAR(255),
                           field_of_study VARCHAR(255),
                           start_date DATE,
                           end_date DATE,
                           current BOOLEAN DEFAULT FALSE
);

CREATE TABLE experience (
                            id BIGSERIAL PRIMARY KEY,
                            cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                            company_name VARCHAR(255) NOT NULL,
                            position VARCHAR(255) NOT NULL,
                            description TEXT,
                            start_date DATE,
                            end_date DATE,
                            current BOOLEAN DEFAULT FALSE
);

CREATE TABLE skills (
                        id BIGSERIAL PRIMARY KEY,
                        cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                        name VARCHAR(100) NOT NULL,
                        level VARCHAR(20) DEFAULT 'INTERMEDIATE'
);