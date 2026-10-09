# Task Tracker
![CI](https://github.com/Narural/TaskTrackerApplication/actions/workflows/ci.yml/badge.svg)
![tests](https://img.shields.io/badge/tests-156-brightgreen)
![coverage](https://img.shields.io/badge/coverage-93%25-brightgreen)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)

REST API трекера задач с пользователями: регистрация, вход по JWT, у каждой задачи есть владелец,
роль ADMIN видит всё.

## Запуск

Необходимы: Java 25, запущенный Docker.

База для приложения:

```
docker run -d --name tasktracker-db -e POSTGRES_PASSWORD=devpass -e POSTGRES_DB=Tasks -p 5433:5432 postgres:18
```

Две переменные окружения — в репозитории их нет:

| Переменная | Что это |
| :--- | :--- |
| `DB_PASSWORD` | пароль базы (`devpass` для контейнера выше) |
| `JWT_SECRET` | секрет подписи токенов, не короче 32 байт — с более коротким приложение не стартует |

Сгенерировать секрет в PowerShell:

```
$bytes = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); [Convert]::ToBase64String($bytes)
```

Тестам переменные не нужны: база поднимается в контейнере Testcontainers, тестовый секрет лежит
в `src/test/resources/application.properties`.

```
./mvnw clean verify
./mvnw test -Dtest=TaskServiceTest
./mvnw test -Dtest=TaskServiceTest#createTask_always_savesTaskWithStatusNewAndOwnerFromCaller
```

## Доступ

### Кто что может

| Эндпоинт | Кто | Владение |
| :--- | :--- | :--- |
| `POST /api/auth/register` | все, без токена | — |
| `POST /api/auth/login` | все, без токена | — |
| `GET /api/me` | вошедший | только о себе |
| `GET /api/tasks` | вошедший | только свои задачи; фильтры `status`, `priority`, `title`, `tag`, пагинация |
| `POST /api/tasks` | вошедший | владелец — тот, кто пришёл с токеном, не из тела запроса |
| `GET /api/tasks/{id}` | вошедший | только своя; чужая — 404, как несуществующая |
| `PATCH /api/tasks/{id}/status` | вошедший | только своя |
| `PATCH /api/tasks/{id}/priority` | вошедший | только своя |
| `POST /api/tasks/{id}/tags` | вошедший | только своя |
| `DELETE /api/tasks/{id}/tags/{tag}` | вошедший | только своя |
| `DELETE /api/tasks/{id}` | вошедший | только своя |
| `GET /api/tasks/statistic` | вошедший | только по своим задачам |
| `GET /api/admin/tasks` | роль `ADMIN` | все задачи всех пользователей |
| всё остальное | никто | `anyRequest().denyAll()`: без токена `401`, с токеном `403` |

Где что проверяется:

- **цепочка фильтров** (`SecurityConfig`) — пускать ли к адресу: без токена `401`; на `/api/admin/**`
  без роли `ADMIN` — `403`, даже если у метода забыли `@PreAuthorize`;
- **`@PreAuthorize`** (`AdminTaskController`) — второй рубеж той же проверки роли: `403`;
- **сервис и репозиторий** (`TaskService`, `findByIdAndOwnerUsername`) — своя ли строка; условие по
  владельцу уходит в SQL, чужие строки базу не покидают.

На чужую задачу отвечаем `404`, а не `403`: ответ не подтверждает, что задача с таким id существует.

### Как получить токен и передать его

1. Зарегистрироваться:

```
POST /api/auth/register
Content-Type: application/json

{"username": "anna", "password": "password1"}
```

2. Войти — в ответе токен и срок жизни в секундах (15 минут):

```
POST /api/auth/login
Content-Type: application/json

{"username": "anna", "password": "password1"}
```
```
{"token": "eyJhbGciOiJIUzI1NiJ9...", "expiresIn": 900}
```

3. Передавать токен в заголовке каждого запроса:

```
GET /api/tasks
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Пароль — от 8 до 70 символов и не длиннее 72 байт в UTF-8: это предел BCrypt, а русская буква
занимает 2 байта, поэтому из русских букв допустимо не больше 36. Длиннее — `400`.

Регистрация всегда выдаёт роль `USER`. Администратора назначают в базе:

```
update users set role = 'ADMIN' where username = 'anna';
```

Все запросы, включая попытки добраться до чужих данных, — в `requests.http`.

## Таблица тестов

| Уровень | Класс | Что проверяет | Тестов |
| :--- | :--- | :--- | ---: |
| Юнит - правила переходов | `model/TaskStatusTest` | допустимые переходы статусов, параметризованно | 13 |
| Юнит - доменный объект | `model/TaskTest` | новая задача получает `NEW`, теги не дублируются | 6 |
| Юнит - валидаторы | `validation/TaskStatusValidatorTest`<br>`validation/TaskPriorityValidatorTest`<br>`validation/MaxUtf8BytesValidatorTest` | допустимые значения статуса и приоритета, длина пароля в байтах | 26 |
| Юнит - сборка DTO | `dto/TaskStatisticTest` | нули по пустым категориям, `totalTasks` как сумма | 4 |
| Юнит - пароли | `security/PasswordEncoderTest` | соль, `matches`, хеш не равен паролю | 3 |
| Юнит - токены | `security/JwtServiceTest` | содержимое токена, срок 15 минут, короткий секрет не принимается | 4 |
| Сервис на моках | `service/TaskServiceTest` | сценарии, владелец из вызывающего, чужая задача — 404 | 13 |
| Веб-срез | `controller/TaskControllerTest` | контракт HTTP через настоящую цепочку безопасности; 405 и 415 вместо 500 | 26 |
| Веб-срез - доступ | `security/TaskAccessWebTest` | 401 без токена, 403 без роли, сервис не вызван; `denyAll` и роль в цепочке | 8 |
| Срез данных | `repository/TaskRepositoryTest` | SQL, фильтр по владельцу и тегу, каскад, констрейнты, миграции | 13 |
| Интеграционные | `integration/TaskApiIntegrationTest` | сквозные сценарии на живом приложении и базе | 6 |
| Интеграционные | `integration/TransactionRollbackTest` | транзакция откатывается целиком | 2 |
| Интеграционные - вход | `integration/AuthApiIntegrationTest` | регистрация, хеш в базе, одинаковый ответ на отказ во входе, пароль длиннее 72 байт | 10 |
| Интеграционные - токены | `integration/TokenAccessIntegrationTest` | без токена, чужой секрет, истёкший токен | 4 |
| Интеграционные - доступ | `security/TaskAccessIntegrationTest` | два пользователя: чужое не видно и не меняется | 17 |
| Контекст | `DemoApplicationTests` | контекст приложения поднимается | 1 |
| **Итого** | | | **156** |

Тестов доступа — 32 (`security/*Access*`, `TokenAccessIntegrationTest`, отказы во входе в
`AuthApiIntegrationTest`), из них отрицательных — 26. Каждый отрицательный тест на изменение
проверяет не только код ответа, но и то, что строка в базе не изменилась.

## Пирамида тестов

| Уровень | Что поднимается | Что подделано | Тестов |
| :--- | :--- | :--- | ---: |
| Юнит | ничего, обычные объекты | - | 56 |
| Сервис на моках | ничего, Mockito | репозитории | 13 |
| Срез данных `@DataJpaTest` | контейнер Postgres, JPA | веб-слоя нет | 13 |
| Веб-срез `@WebMvcTest` | MVC, Jackson, валидация, Spring Security | сервис | 34 |
| Интеграционные `@SpringBootTest` | всё приложение и контейнер | ничего | 40 |
| **Итого** | | | **156** |
