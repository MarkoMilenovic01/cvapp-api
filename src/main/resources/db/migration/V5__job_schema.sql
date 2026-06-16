CREATE TABLE jobs (
                      id BIGSERIAL PRIMARY KEY,

                      company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,

                      title VARCHAR(255) NOT NULL,
                      description TEXT NOT NULL,
                      requirements TEXT,
                      location VARCHAR(255),

                      employment_type VARCHAR(50) NOT NULL,
                      work_mode VARCHAR(50) NOT NULL,

                      deadline DATE,
                      active BOOLEAN NOT NULL DEFAULT TRUE,

                      created_at TIMESTAMP DEFAULT NOW(),
                      updated_at TIMESTAMP DEFAULT NOW()
);