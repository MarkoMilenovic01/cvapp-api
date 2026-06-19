CREATE TABLE password_reset_tokens(
                                       id BIGSERIAL PRIMARY KEY,
                                       email VARCHAR(255) NOT NULL,
                                       token VARCHAR(255) NOT NULL UNIQUE,
                                       used BOOLEAN NOT NULL DEFAULT FALSE,
                                       expires_at TIMESTAMP NOT NULL,
                                       created_at TIMESTAMP DEFAULT NOW()
);