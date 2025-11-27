# Backend банковской системы

Spring Boot‑приложение для учебного фронтенда: авторизация, сотрудники/роли, демо‑счета и простые операции (без настоящей безопасности). Данные лежат в PostgreSQL, схема создаётся JPA/Hibernate.

## Технический стек
- Java 25
- Spring Boot 4.0.0 (Web MVC, Data JPA)
- PostgreSQL
- Maven Wrapper (`./mvnw`)

## Требования
- Установленный JDK (рекомендуется 21+; проект сейчас настроен на 25)
- PostgreSQL, доступный на `localhost:5434`
- Созданная база данных `banksys` и пользователь `taurbek` (пароль пустой по умолчанию)

### Подготовка базы данных (пример)
```bash
psql -p 5434 -U taurbek -c "CREATE DATABASE banksys;"
```
Если используете другой порт/логин/пароль, укажите их в конфигурации ниже.

## Конфигурация
Основные настройки находятся в `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/banksys
spring.datasource.username=taurbek
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
```
- Любую из настроек можно перекрыть переменными окружения (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SERVER_PORT`, и т.д.).
- `ddl-auto=update` автоматически создаст таблицы (`employees`, `roles`, `employee_roles`) при первом запуске.

## Запуск
```bash
cd backend
./mvnw spring-boot:run
# или собрать jar
./mvnw clean package
java -jar target/banksys-0.0.1-SNAPSHOT.jar
```
Приложение поднимется на `http://localhost:8080`.

## Данные по умолчанию
`DataInitializer` создаёт роли и пользователей, если их нет:
- `employee1` / `password` — USER
- `manager1` / `password` — USER, MANAGER
- `admin1` / `admin` — USER, MANAGER, ADMIN
- `demo1` / `demo`, `demo2` / `demo` — USER

Имена: Еркебулан, Аслан, Магжан, Жанибек, Айбол. Токен — строка `mock-<id>`.

Для **каждого пользователя** при старте создаётся один KZT‑счёт (баланс/долг зависят от логина). Счёты хранятся в памяти, пересоздаются при рестарте.

## API (основное)
- `POST /api/auth/login` — логин `username`/`password`, ответ: `token` + `employee`.
- `GET /api/employees/me` — профиль по заголовку `Authorization: Bearer mock-<id>`.
- `GET /api/employees/users` — список всех сотрудников (для подсказок получателей).

### Демо-счета и операции (in‑memory)
- `GET /api/accounts` — список счетов (для обычного USER фронт фильтрует только свой счёт).
- `POST /api/accounts/transfer/by-user` — перевод по логинам:
  ```json
  {
    "fromUsername": "demo1",
    "toUsername": "demo2",
    "amount": 5000,
    "description": "Тест перевод"
  }
  ```
  Ответ 200: `TransferResponse` с обновлёнными счетами. При нехватке средств — 409.  
  **Ограничение:** переводы заблокированы для ролей ADMIN, MANAGER, EMPLOYEE (будет 409 с сообщением).

- `POST /api/accounts/loan` — оформить кредит на счёт:
  ```json
  { "accountId": 1, "amount": 20000, "termMonths": 12, "rate": 0.12 }
  ```
  Баланс пополняется на `amount`, долг растёт на `amount * (1 + rate)`.  
  **Ограничение:** кредиты недоступны для ADMIN/MANAGER/EMPLOYEE (409 с сообщением).

- `GET /api/accounts/transfers?user=<username>` — история переводов/кредитов (фильтр по пользователю или весь журнал).

## Архитектура пакетов
- `controller` — REST-эндпойнты (`AuthController`, `EmployeeController`, `AccountController`)
- `service` — сервис авторизации `AuthTokenService` (mock-токены) и демо-логика по счетам/операциям `AccountDemoService`
- `repository` — JPA-репозитории для `Employee` и `Role`
- `model` — JPA-сущности `Employee`, `Role`
- `dto` — объекты для ответов/запросов (`LoginRequest`, `LoginResponse`, `EmployeeDto`)
- `mapper` — преобразование сущностей в DTO (`EmployeeMapper`)
- `config` — `DataInitializer` с начальными ролями/пользователями

## Особенности и ограничения
- Балансы/счета/история — только в памяти; при рестарте пересоздаются.
- Пароли в открытом виде, токен mock без срока действия.
- CORS открыт для всех доменов.
- Переводы/кредиты разрешены только роль USER; остальные роли увидят ошибку.
- Нет автоматических тестов.

## Полезные команды
- `./mvnw spring-boot:run` — запуск приложения
- `./mvnw clean package` — сборка `target/banksys-0.0.1-SNAPSHOT.jar`
- `./mvnw test` — запуск тестов (сейчас тестов нет, команда завершится быстро)
