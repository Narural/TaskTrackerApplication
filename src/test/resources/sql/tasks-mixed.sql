insert into users (username, password_hash, role) values ('sql-owner', 'not-a-real-hash', 'USER');

insert into tasks (title, description, priority, status, created_at, owner_id) values
    ('Задача 1', 'a', 'HIGH',     'NEW',        now(), (select id from users where username = 'sql-owner')),
    ('Задача 2', 'a', 'HIGH',     'NEW',        now(), (select id from users where username = 'sql-owner')),
    ('Задача 3', 'a', 'LOW',      'IN_PROCESS', now(), (select id from users where username = 'sql-owner')),
    ('Задача 4', 'a', 'MEDIUM',   'DONE',       now(), (select id from users where username = 'sql-owner')),
    ('Задача 5', 'a', 'CRITICAL', 'DONE',       now(), (select id from users where username = 'sql-owner'));
