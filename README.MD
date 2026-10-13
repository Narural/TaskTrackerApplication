# Task Tracker
![CI](https://github.com/Narural/TaskTrackerApplication/actions/workflows/ci.yml/badge.svg)
![tests](https://img.shields.io/badge/tests-170-brightgreen)
![coverage](https://img.shields.io/badge/coverage-93%25-brightgreen)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)

REST API трекера задач с пользователями: регистрация, вход по JWT, у каждой задачи есть владелец,
роль ADMIN видит всё.

## Запуск в Docker

Нужен только Docker.

```
git clone https://github.com/Narural/TaskTrackerApplication.git && cd TaskTrackerApplication
cp .env.example .env        # заполнить DB_PASSWORD, JWT_SECRET (не короче 32 символов), ADMIN_PASSWORD
docker compose up -d --wait
```

Приложение — http://localhost:8080. Первый администратор `admin` с паролем из `ADMIN_PASSWORD` создаётся
при старте сам. `up --wait` возвращает управление, когда база и приложение получили статус `healthy`.

Остановить: `docker compose down` — данные остаются в томе; `docker compose down -v` — вместе с данными.
Логи: `docker compose logs app`, в профиле `prod` они в формате JSON.

## Настройки

Одинаковое на любой машине лежит в `application.properties`. То, что зависит от назначения окружения
(уровни и формат логов), — в `application-dev.properties` и `application-prod.properties`. Секреты и
всё, что отличается от запуска к запуску, приходит из переменных окружения, в Docker — из `.env`.

| Настройка | Где задаётся | По умолчанию | Если не задать |
| :--- | :--- | :--- | :--- |
| `DB_URL` | окружение; в Compose `jdbc:postgresql://db:5432/tasks` | `jdbc:postgresql://localhost:5433/Tasks` | берётся адрес по умолчанию; внутри контейнера там базы нет — не стартует, `Connection to localhost:5433 refused` |
| `DB_USER` | окружение | `postgres` | `postgres` |
| `DB_PASSWORD` | окружение, `.env` | нет | Compose отказывается стартовать; без Compose — `password authentication failed` |
| `JWT_SECRET` | окружение, `.env` | нет | Compose отказывается стартовать; без Compose — `APPLICATION FAILED TO START`, `секрет короче 32 символов` |
| `security.jwt.lifetime` (`SECURITY_JWT_LIFETIME`) | `application.properties`, переопределяется окружением | `15m` | — ; нечитаемое значение (`abc`) — не стартует; число без единицы — миллисекунды |
| `ADMIN_USERNAME` | окружение | `admin` | `admin` |
| `ADMIN_PASSWORD` | окружение, `.env` | пусто | администратор не создаётся, в логе `WARN`; Compose отказывается стартовать |
| `SPRING_PROFILES_ACTIVE` | окружение; в Compose `prod` | не задан | работают только общие настройки: без `DEBUG`, логи строками |
| `management.endpoints.web.exposure.include` | `application.properties` | `health,info,metrics` | — |
| `management.endpoint.health.*` | `application.properties` | liveness и readiness включены, база входит в readiness, подробности только `ADMIN` | — |
| `spring.datasource.hikari.connection-timeout` | `application.properties` | `3000` мс | без настройки запрос при упавшей базе ждал бы 30 секунд |
| `logging.level.*`, `logging.pattern.level` | `application-dev.properties` | `DEBUG` для SQL и Security, идентификатор запроса в строке | — |
| `logging.structured.format.console` | `application-prod.properties` | `ecs` — JSON | — |

Секретам значение по умолчанию не дано намеренно: значение по умолчанию лежало бы в репозитории и
было бы известно всем, а забытая переменная не остановила бы приложение. У тестов свой
`src/test/resources/application.properties`, он заменяет основной целиком, поэтому новые настройки
дублируются и туда.

## Запуск для разработки

Необходимы: Java 25, запущенный Docker.

База для приложения:

```
docker run -d --name tasktracker-db -e POSTGRES_PASSWORD=devpass -e POSTGRES_DB=Tasks -p 5433:5432 postgres:18
```

Переменные окружения — в репозитории их нет:

| Переменная | Что это |
| :--- | :--- |
| `DB_PASSWORD` | пароль базы (`devpass` для контейнера выше) |
| `JWT_SECRET` | секрет подписи токенов, не короче 32 символов — с более коротким приложение не стартует |
| `ADMIN_PASSWORD` | пароль первого администратора; не задан — администратор не создаётся |
| `SPRING_PROFILES_ACTIVE` | `dev` — подробные логи SQL и Security с идентификатором запроса |

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
| `GET /actuator/health` | все, без токена | аноним видит только `status`, `ADMIN` — ещё и компоненты |
| `GET /actuator/health/liveness`, `/actuator/health/readiness` | все, без токена | readiness учитывает базу, liveness — нет |
| `GET /actuator/info`, `/actuator/metrics` | роль `ADMIN` | без токена `401`, `USER` — `403` |
| любой другой `/actuator/...` | никто | не опубликован: без токена `401`, `ADMIN` — `404` |
| всё остальное | никто | `anyRequest().denyAll()`: без токена `401`, с токеном `403` |

Где что проверяется:

- **цепочка фильтров** (`SecurityConfig`) — пускать ли к адресу: без токена `401`; на `/api/admin/**`
  и `/actuator/**` без роли `ADMIN` — `403`, даже если у метода забыли `@PreAuthorize`;
- **`@PreAuthorize`** (`AdminTaskController` и `TaskService.searchAll`) — та же проверка роли на
  контроллере и на методе сервиса: `403`;
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

Имя пользователя обрезается по краям и приводится к нижнему регистру: `Anna` и ` anna ` — один и
тот же пользователь.

Регистрация всегда выдаёт роль `USER`. Первый администратор создаётся при старте приложения из
`ADMIN_USERNAME` (по умолчанию `admin`) и `ADMIN_PASSWORD`, если пользователя с таким именем ещё нет.
Пароль хешируется так же, как при регистрации, и в лог не попадает.

Все запросы, включая попытки добраться до чужих данных и адреса Actuator, — в `requests.http`.
Перед запуском впиши в `@adminPassword` в начале файла тот же пароль, что в `ADMIN_PASSWORD`.

## Если не стартует

| Что видно | Причина | Что сделать |
| :--- | :--- | :--- |
| `required variable JWT_SECRET is missing a value: задай JWT_SECRET в .env` | в `.env` нет переменной | заполнить `.env` по образцу `.env.example` |
| `APPLICATION FAILED TO START` … `Property: security.jwt.secret` … `секрет короче 32 символов` | секрет короче 32 символов | сгенерировать длинный, а не придумывать руками |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | порт 8080 занят другим приложением или стеком | `docker ps`, остановить то, что держит порт |

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
| Сервис на моках - запуск | `config/AdminBootstrapTest` | первый администратор: создаётся с хешем, не дублируется, пароль не в логе | 3 |
| Веб-срез | `controller/TaskControllerTest` | контракт HTTP через настоящую цепочку безопасности; 405 и 415 вместо 500 | 26 |
| Веб-срез - доступ | `security/TaskAccessWebTest` | 401 без токена, 403 без роли, сервис не вызван; `denyAll` и роль в цепочке | 8 |
| Срез данных | `repository/TaskRepositoryTest` | SQL, фильтр по владельцу и тегу, каскад, констрейнты, миграции | 13 |
| Интеграционные | `integration/TaskApiIntegrationTest` | сквозные сценарии на живом приложении и базе | 6 |
| Интеграционные | `integration/TransactionRollbackTest` | транзакция откатывается целиком | 2 |
| Интеграционные | `integration/OptimisticLockIntegrationTest` | устаревшая копия задачи не затирает чужое изменение | 1 |
| Интеграционные - вход | `integration/AuthApiIntegrationTest` | регистрация, хеш в базе, одинаковый ответ на отказ во входе, пароль длиннее 72 байт, имя без пробелов и в нижнем регистре | 13 |
| Интеграционные - токены | `integration/TokenAccessIntegrationTest` | без токена, чужой секрет, истёкший токен | 4 |
| Интеграционные - доступ | `security/TaskAccessIntegrationTest` | два пользователя: чужое не видно и не меняется | 17 |
| Интеграционные - доступ | `security/ServiceMethodSecurityIntegrationTest` | роль `ADMIN` проверяется на методе сервиса | 2 |
| Эксплуатация | `ops/ActuatorAccessIntegrationTest` | health анониму без подробностей, info: 401 / 403 / 200 | 4 |
| Эксплуатация | `ops/LogSecretsIntegrationTest` | после регистрации, входа и запроса в логе нет пароля и токена | 1 |
| Контекст | `DemoApplicationTests` | контекст приложения поднимается | 1 |
| **Итого** | | | **170** |

Тестов доступа — 38 (`security/*Access*`, `security/ServiceMethodSecurityIntegrationTest`,
`ops/ActuatorAccessIntegrationTest`, `TokenAccessIntegrationTest`, отказы во входе в
`AuthApiIntegrationTest`), из них отрицательных — 29. Каждый отрицательный тест на изменение
проверяет не только код ответа, но и то, что строка в базе не изменилась.

## Пирамида тестов

| Уровень | Что поднимается | Что подделано | Тестов |
| :--- | :--- | :--- | ---: |
| Юнит | ничего, обычные объекты | - | 56 |
| Сервис на моках | ничего, Mockito | репозитории | 16 |
| Срез данных `@DataJpaTest` | контейнер Postgres, JPA | веб-слоя нет | 13 |
| Веб-срез `@WebMvcTest` | MVC, Jackson, валидация, Spring Security | сервис | 34 |
| Интеграционные `@SpringBootTest` | всё приложение и контейнер | ничего | 51 |
| **Итого** | | | **170** |
