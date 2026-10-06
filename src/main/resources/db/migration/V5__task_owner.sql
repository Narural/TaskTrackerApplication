-- У задачи появляется владелец.
-- NOT NULL сразу поставить нельзя: в уже существующих строках колонка была бы пустой.
-- Порядок: добавить nullable -> заполнить -> ужесточить.

ALTER TABLE tasks ADD COLUMN owner_id BIGINT;

-- Существующим задачам нужен владелец. Отдаём их первому администратору.
-- Если задачи есть, а администратора нет, заводим служебного пользователя,
-- под которым войти нельзя: '!' не является хешем BCrypt, matches() для него всегда false.
INSERT INTO users (username, password_hash, role)
SELECT 'legacy-owner', '!', 'ADMIN'
WHERE EXISTS (SELECT 1 FROM tasks)
  AND NOT EXISTS (SELECT 1 FROM users WHERE role = 'ADMIN');

UPDATE tasks
SET owner_id = (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id LIMIT 1)
WHERE owner_id IS NULL;

ALTER TABLE tasks ALTER COLUMN owner_id SET NOT NULL;

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_owner
    FOREIGN KEY (owner_id) REFERENCES users (id);

-- Каждый список задач теперь фильтруется по владельцу.
CREATE INDEX idx_tasks_owner ON tasks (owner_id);
