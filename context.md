# BANKsys — подробный контекст проекта

Документ о том, как устроен учебный BANKsys: стек, файлы, потоки данных, работа токена и фронта.

## 1. Что это за проект
- Бэкенд: Java 17+/Spring Boot 4.0.0, PostgreSQL, Maven.
- Фронтенд: Next.js 16 (app router), React 19.
- Хранит сотрудников и их роли, авторизация через простой мок-токен вида `mock-<id>`.

## 2. Ключевые файлы и пакеты (бэкенд)
- `backend/src/main/java/com/example/banksys/BanksysApplication.java` — точка входа Spring Boot.
- `backend/src/main/resources/application.properties` — настройки БД и порта.
- Модели (`com.example.banksys.model`):
  - `Employee` — таблица `employees`, поля: id, fullName, username, password (plain), is_active, `@ManyToMany` роли.
  - `Role` — таблица `roles`, поля: id, name, description, связь с сотрудниками.
- Репозитории (`com.example.banksys.repository`): `EmployeeRepository` (поиск по username), `RoleRepository` (поиск по name).
- Сервисы (`com.example.banksys.service`): `AuthTokenService` — генерирует/парсит токены `mock-<employeeId>`.
- DTO (`com.example.banksys.dto`): `LoginRequest`, `LoginResponse`, `EmployeeDto`.
- Мапперы (`com.example.banksys.mapper`): `EmployeeMapper` — конвертирует сущность в DTO с ролями-строками.
- Контроллеры (`com.example.banksys.controller`):
  - `AuthController` — `POST /api/auth/login` (выдаёт токен и данные сотрудника).
  - `EmployeeController` — `GET /api/employees/me` (возвращает текущего сотрудника по токену).
- Конфиг/данные (`com.example.banksys.config`):
  - `DataInitializer` — при старте создаёт роли EMPLOYEE/MANAGER/ADMIN и трёх пользователей.

## 3. Конфигурация окружения
Файл `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/banksys
spring.datasource.username=taurbek
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
server.port=8080
```
- Порт БД подставляйте свой (5432 по умолчанию в Postgres).
- `ddl-auto=update` — таблицы создаются/обновляются автоматически по моделям.

## 4. База данных
Таблицы (создаются JPA):
- `roles(id, name, description)`
- `employees(id, full_name, username, password, is_active)`
- `employee_roles(employee_id, role_id)` — связь многие-ко-многим.

Инициализированные данные (`DataInitializer`):
- Роли: EMPLOYEE, MANAGER, ADMIN.
- Пользователи:
  - employee1 / password — роль EMPLOYEE
  - manager1 / password — роли EMPLOYEE, MANAGER
  - admin1 / admin — роли EMPLOYEE, MANAGER, ADMIN

## 5. Поток авторизации и токены
### 5.1 Логин (`POST /api/auth/login`)
- Принимает JSON: `{"username": "...", "password": "..."}`.
- Контроллер проверяет наличие полей, ищет пользователя по username, сверяет пароль (plain text) и `is_active`.
- Генерирует токен через `AuthTokenService.generateToken(id)` → строка `mock-<id>`.
- Ответ: `{"token": "mock-3", "employee": {id, fullName, username, roles[]}}`.
- При ошибке логина/пароля/неактивности — `401 UNAUTHORIZED`.

### 5.2 Получить текущего (`GET /api/employees/me`)
- Требуется заголовок `Authorization: Bearer mock-<id>`.
- `AuthTokenService.parseEmployeeId` достаёт id из токена (обрезает префикс `mock-` и парсит Long).
- Репозиторий ищет пользователя по id; если нет — `401`.
- Успешно: возвращает `EmployeeDto` с ролями-строками.

### 5.3 Безопасность (ограничения учебного режима)
- Токен простой, не JWT, не подписан и не истекает — годится только для учебных/локальных целей.
- Пароли в базе в открытом виде (для демо).

## 6. Фронтенд (Next.js)
- Расположение: `frontend/banksys`.
- Базовый URL бэка: `NEXT_PUBLIC_API_BASE_URL` (пример в `.env.local.example`), по умолчанию `http://localhost:8080`.
- Главная страница (`app/page.tsx`):
  - Поля логина/пароля с предзаполнением `admin1/admin`.
  - Кнопка логина → `POST /api/auth/login`; токен сохраняется в `localStorage` как `banksys-token`.
  - Кнопка “Запросить /me” → `GET /api/employees/me` с токеном.
  - Отображение токена (маскировано) и карточка профиля с ролями.
- Глобальные стили/шрифты: `app/globals.css`, `app/layout.tsx` (Space Grotesk + JetBrains Mono).

## 7. Запуск и проверка
- Бэкенд: `cd backend && mvn spring-boot:run` → http://localhost:8080.
- Фронтенд: `cd frontend/banksys && npm run dev` → http://localhost:3000.
- Проверка логина через curl:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin1","password":"admin"}'
```
- Проверка /me с токеном:
```bash
curl -H "Authorization: Bearer mock-3" http://localhost:8080/api/employees/me
```

## 8. Как посмотреть данные в Postgres (macOS)
1. Убедитесь, что Postgres запущен (brew service или контейнер).
2. Подключитесь:
```bash
psql -h localhost -p 5434 -U taurbek -d banksys
```
3. Внутри `psql`:
```
\dt
SELECT * FROM employees;
SELECT * FROM roles;
SELECT * FROM employee_roles;
\q
```
Если `psql` отсутствует: `brew install libpq` и добавьте в PATH `export PATH="/opt/homebrew/opt/libpq/bin:$PATH"`.

## 9. Как это работает целиком (шаги)
1. При старте приложения Spring Boot вызывает `DataInitializer`: создаёт роли/пользователей, если их нет.
2. Пользователь вводит логин/пароль на фронте → `POST /api/auth/login`.
3. Бэк проверяет пользователя, генерирует `mock-<id>`, отдаёт его вместе с DTO сотрудника.
4. Фронт сохраняет токен в `localStorage` и показывает карточку.
5. При нажатии на “/me” фронт шлёт `GET /api/employees/me` с заголовком Bearer, бэк парсит `mock-<id>`, достаёт сотрудника и отдаёт DTO.
6. Роли из ответа можно использовать на фронте для ограничения UI (EMPLOYEE/MANAGER/ADMIN).

## 10. Идеи расширения (куда двигаться)
- Заменить мок-токен на JWT (с подписью, exp, refresh).
- Захешировать пароли (BCrypt) и добавить регистрацию/смену пароля.
- Добавить сущности Client/Account/Transaction и ролевые ограничения на операции.
- Ввести фильтр/интерсептор, который автоматически проверяет токен вместо ручной проверки в контроллерах.
- Вынести конфиги окружения в профили (dev/test/prod) и Docker Compose для БД.

## 11. Полезные настройки и переменные
- Бэкенд порт: `server.port` (по умолчанию 8080).
- База: `spring.datasource.url` (замените порт/хост), `spring.datasource.username`, `spring.datasource.password`.
- JPA стратегия: `spring.jpa.hibernate.ddl-auto=update` (для прод рекомендуется `validate`/миграции).
- Фронт: `NEXT_PUBLIC_API_BASE_URL` в `.env.local` (пример лежит в `.env.local.example`).

## 12. Структура фронта и логика страниц
- `app/layout.tsx` — метаданные, подключение шрифтов (Space Grotesk, JetBrains Mono), глобальные стили.
- `app/globals.css` — фон, цвета, переменные темing, базовый шрифт.
- `app/page.tsx` — единственная страница:
  - `username/password` стейт, предзаполнены `admin1/admin`.
  - Кнопка “Войти и получить токен” → fetch `/api/auth/login`.
  - Кнопка “Запросить /me” → fetch `/api/employees/me` с сохраненным токеном.
  - Токен хранится в `localStorage` (`banksys-token`), при загрузке пытается восстановиться.
  - Карточка выводит маскированный токен и данные сотрудника (ФИО, логин, id, роли).
  - `API_BASE` вычисляется из `NEXT_PUBLIC_API_BASE_URL` или `http://localhost:8080`.

## 13. Частые ошибки и проверки
- 401 при логине: неправильный логин/пароль или `is_active=false`.
- 401 при `/me`: нет заголовка `Authorization`, токен без префикса `mock-`, неверный id, пользователь удален.
- Проблемы с БД: неверный порт/логин/пароль в `application.properties`, сервер Postgres не запущен.
- CORS: контроллеры имеют `@CrossOrigin("*")` — для учебных целей, на прод нужно сужать.
- Если данные не обновляются: при `ddl-auto=update` таблицы не пересоздаются; дропните строки/таблицы вручную или используйте миграции.

## 14. Как добавить/изменить пользователей и роли
- Через код: правьте `DataInitializer` (логины/пароли/ФИО/роли) и перезапускайте бэк. Для чистого состояния удалите записи из таблиц `employees`, `employee_roles`, `roles` или дропните их.
- Через SQL (psql): `INSERT INTO roles ...`, `INSERT INTO employees ...`, затем заполните `employee_roles`.
- Новые роли: добавьте в таблицу `roles` (name, description) и в `employee_roles` свяжите с пользователями. Код автоматически отобразит роли в `/me`.

## 15. Мини-план по безопасности (если развивать)
- Пароли: хранить в хэше (BCrypt), убрать plain text.
- Токены: перейти на JWT с подписью, временем жизни и refresh-механизмом.
- Авторизация: вынести проверку токена в фильтр/интерсептор, использовать `SecurityContext`.
- Логи: не писать чувствительные данные в логи.
- CORS: ограничить домены фронта, убрать `*`.
- Переменные окружения: секреты держать вне git, использовать профили/секрет-хранилища.
