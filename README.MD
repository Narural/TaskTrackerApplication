#Task Tracker
![CI](https://github.com/Narural/TaskTrackerApplication/actions/workflows/ci.yml/badge.svg)
![tests](https://img.shields.io/badge/tests-97-brightgreen)
![coverage](https://img.shields.io/badge/coverage-88%25-brightgreen)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
Необходимы: Java 25, Запущенный Docker
./mvnw test
./mvnw clean verify
./mvnw test -Dtest=TaskServiceTest
./mvnw test -Dtest=TaskServiceTest#createTask_always_savesTaskWithStatusNew
## Таблица тестов
| Уровень | Класс | Что проверяет | Тестов |
| :--- | :--- | :--- | ---: |
| Юнит — правила переходов | `model/TaskStatusTest` | допустимые переходы статусов, параметризованно | 13 |
| Юнит — доменный объект | `model/TaskTest` | новая задача получает `NEW`, теги не дублируются | 6 |
| Юнит — валидаторы | `validation/TaskStatusValidatorTest`<br>`validation/TaskPriorityValidatorTest` | допустимые значения статуса и приоритета | 22 |
| Юнит — сборка DTO | `dto/TaskStatisticTest` | нули по пустым категориям, `totalTasks` как сумма | 4 |
| Сервис на моках | `service/TaskServiceTest` | сценарии и ветвления, репозиторий подменён | 12 |
| Веб-срез | `controller/TaskControllerTest` | контракт HTTP: коды, тела, формат ошибок | 22 |
| Срез данных | `repository/TaskRepositoryTest` | SQL, схема, каскад, констрейнты, число запросов, миграции | 9 |
| Интеграционные | `integration/TaskApiIntegrationTest` | сквозные сценарии на живом приложении и базе | 6 |
| Интеграционные | `integration/TransactionRollbackTest` | транзакция откатывается целиком | 2 |
| Контекст | `DemoApplicationTests` | контекст приложения поднимается | 1 |
| **Итого** | | | **97** |

## Пирамида тестов

| Уровень | Что поднимается | Что подделано | Тестов | Время |
| :--- | :--- | :--- | ---: | ---: |
| Юнит | ничего, обычные объекты | — | 45 | 1,2 с |
| Сервис на моках | ничего, Mockito | репозиторий | 12 | 0,7 с |
| Срез данных `@DataJpaTest` | контейнер Postgres, JPA | веб-слоя нет | 9 | 5,9 с |
| Веб-срез `@WebMvcTest` | MVC, Jackson, валидация | сервис | 22 | 13,0 с |
| Интеграционные `@SpringBootTest` | всё приложение и контейнер | ничего | 9 | 25,8 с |
| **Итого** | | | **97** | **~47 с** |

