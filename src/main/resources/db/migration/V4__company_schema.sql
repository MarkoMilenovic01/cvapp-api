CREATE TABLE companies (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           website VARCHAR(255),
                           industry VARCHAR(100),
                           created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE cv_views (
                          id BIGSERIAL PRIMARY KEY,
                          company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
                          cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                          viewed_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE favorite_cvs (
                              company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
                              cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                              PRIMARY KEY (company_id, cv_id)
);