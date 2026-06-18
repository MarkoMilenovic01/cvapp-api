CREATE TABLE company_invites (
                                 id BIGSERIAL PRIMARY KEY,
                                 email VARCHAR(255) NOT NULL UNIQUE,
                                 token VARCHAR(255) NOT NULL UNIQUE,
                                 used BOOLEAN NOT NULL DEFAULT FALSE,
                                 expires_at TIMESTAMP NOT NULL,
                                 created_at TIMESTAMP DEFAULT NOW()
);