CREATE TABLE tasks (
                       id          BIGSERIAL PRIMARY KEY,
                       title       VARCHAR(100) NOT NULL,
                       description VARCHAR(100),
                       priority    VARCHAR(20) NOT NULL,
                       status      VARCHAR(20) NOT NULL,
                       created_at  TIMESTAMP NOT NULL
);

CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_priority ON tasks (priority);