CREATE TABLE job_applications (
                                  id BIGSERIAL PRIMARY KEY,

                                  job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
                                  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                  cv_id BIGINT NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,

                                  status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',

                                  applied_at TIMESTAMP DEFAULT NOW(),
                                  updated_at TIMESTAMP DEFAULT NOW(),

                                  CONSTRAINT uq_job_user_application UNIQUE (job_id, user_id)
);