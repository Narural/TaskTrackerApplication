CREATE TABLE users(
                           id          BIGSERIAL PRIMARY KEY,
                           username       VARCHAR(50) NOT NULL,
                           password_hash VARCHAR(100) NOT NULL,
                           role    VARCHAR(20) NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                           CONSTRAINT uq_users_username UNIQUE (username)
)