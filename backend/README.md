# Backend банковской системы

Spring Boot-приложение для управления сотрудниками банка и ролями с простой (mock) аутентификацией для фронтенда. Данные хранятся в PostgreSQL, схема создается автоматически через JPA/Hibernate.

## Технический стек
- Java 25 (см. `java.version` в `pom.xml`; если у вас установлен другой JDK, поменяйте значение или используйте совместимую версию)
- Spring Boot 4.0.0 (Web MVC, Data JPA)
- PostgreSQL (JDBC driver в зависимостях)
- Maven Wrapper (`./mvnw`) для сборки и запуска

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
`DataInitializer` создает роли и трёх сотрудников, если их ещё нет:
- `employee1` / `password` — роли: EMPLOYEE
- `manager1` / `password` — роли: EMPLOYEE, MANAGER
- `admin1` / `admin` — роли: EMPLOYEE, MANAGER, ADMIN

Токен генерируется в формате `mock-<id>` (см. `AuthTokenService`), где `<id>` — идентификатор сотрудника из базы.

## API
- `POST /api/auth/login` — логин по `username`/`password`
  - Пример запроса:
    ```bash
    curl -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"username":"employee1","password":"password"}'
    ```
  - Успешный ответ:
    ```json
    {
      "token": "mock-1",
      "employee": {
        "id": 1,
        "fullName": "Иван Сотрудник",
        "username": "employee1",
        "roles": ["EMPLOYEE"]
      }
    }
    ```

- `GET /api/employees/me` — получение профиля текущего сотрудника
  - Заголовок: `Authorization: Bearer mock-<id>`
  - Пример:
    ```bash
    curl http://localhost:8080/api/employees/me \
      -H "Authorization: Bearer mock-1"
    ```
  - Ответ 200 содержит `EmployeeDto`; при ошибке авторизации возвращается 401.

## Архитектура пакетов
- `controller` — REST-эндпойнты (`AuthController`, `EmployeeController`)
- `service` — сервис авторизации `AuthTokenService` (mock-токены)
- `repository` — JPA-репозитории для `Employee` и `Role`
- `model` — JPA-сущности `Employee`, `Role`
- `dto` — объекты для ответов/запросов (`LoginRequest`, `LoginResponse`, `EmployeeDto`)
- `mapper` — преобразование сущностей в DTO (`EmployeeMapper`)
- `config` — `DataInitializer` с начальными ролями/пользователями

## Особенности и ограничения
- Пароли хранятся в открытом виде; шифрование и полноценная безопасность (JWT, сессии, фильтры) не реализованы.
- Токены — простая строка `mock-<id>` без истечения срока действия и без проверок ролей.
- CORS открыт для всех доменов (`@CrossOrigin(origins = "*")`) для удобства фронтенда.
- Нет автоматических тестов; при необходимости добавьте модульные тесты к контроллерам/сервисам.

## Полезные команды
- `./mvnw spring-boot:run` — запуск приложения
- `./mvnw clean package` — сборка `target/banksys-0.0.1-SNAPSHOT.jar`
- `./mvnw test` — запуск тестов (сейчас тестов нет, команда завершится быстро)
