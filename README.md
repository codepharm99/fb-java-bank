# BANKsys (demo)

Учебный проект «банковская система» из двух частей:
- **backend** — Spring Boot (Java 25, PostgreSQL) с простейшей аутентификацией, ролями и демо-счетами в памяти.
- **frontend** — Next.js 14 (TypeScript/React) с формой входа, просмотром счетов, переводами и кредитованием.

> Важное ограничение: все балансы/счета/история переводов — in‑memory в бэкенде, при рестарте пересоздаются. Реальной безопасности (хеширование паролей, JWT, ACL) нет — это демо.

---

## Быстрый старт

### Backend
1. Требования: JDK 25 (подойдёт 21+), PostgreSQL на `localhost:5434`, БД `banksys`, пользователь `taurbek` без пароля (можно переопределить через переменные окружения).
2. Запуск:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   # или mvn spring-boot:run при установленном Maven
   ```
3. Приложение поднимется на `http://localhost:8080`, таблицы создадутся автоматически (`spring.jpa.hibernate.ddl-auto=update`).

### Frontend
1. Требования: Node.js 18+.
2. Запуск:
   ```bash
   cd frontend/banksys
   npm install
   npm run dev   # для разработки
   # npm run build && npm run start — для прод-сборки
   ```
3. По умолчанию фронт ходит на `http://localhost:8080` (можно задать `NEXT_PUBLIC_API_BASE_URL`).

---

## Пользователи и роли (сид по умолчанию)
- `employee1` / `password` — USER
- `manager1` / `password` — USER, MANAGER
- `admin1` / `admin` — USER, MANAGER, ADMIN
- `demo1` / `demo` — USER
- `demo2` / `demo` — USER

Имена: Еркебулан, Аслан, Магжан, Жанибек, Айбол.  
Каждому при старте создаётся один счёт в KZT (баланс/долг зависят от логина). Счёты и история пересоздаются при рестарте бэкенда.

---

## Backend (подробнее)
- Стек: Spring Boot 4.0.0 (Web MVC, Data JPA), Java 25, PostgreSQL.
- Конфиг: `backend/src/main/resources/application.properties` (URL/логин/пароль БД можно переопределять env-переменными).
- Сид: роли USER/MANAGER/ADMIN; пользователи (employee1/manager1/admin1/demo1/demo2) с казахстанскими именами; каждому создаётся один KZT-счёт в памяти.
- Токен: `mock-<id>`, без срока действия; пароли не хешируются (только для демо).
- CORS открыт для всех доменов.

### API
- `POST /api/auth/login` — логин по `username`/`password`, ответ: `token` (`mock-<id>`) и `employee`.
- `GET /api/employees/me` — профиль по заголовку `Authorization: Bearer mock-<id>`.
- `GET /api/employees/users` — список всех сотрудников (для подсказок получателей).
- `GET /api/accounts` — демо-счета (для USER фронт фильтрует только свой счёт).
- `POST /api/accounts/transfer/by-user` — перевод по логинам:
  ```json
  {
    "fromUsername": "demo1",
    "toUsername": "demo2",
    "amount": 5000,
    "description": "Тест перевод"
  }
  ```
  Ответ 200 — `TransferResponse`, при нехватке средств 409. **Переводы запрещены** для ролей ADMIN/MANAGER/EMPLOYEE (409 с сообщением).
- `POST /api/accounts/loan` — кредит на счёт:
  ```json
  { "accountId": 1, "amount": 20000, "termMonths": 12, "rate": 0.12 }
  ```
  Баланс +amount, долг += amount*(1+rate). **Кредиты запрещены** для ADMIN/MANAGER/EMPLOYEE.
- `GET /api/accounts/transfers?user=<username>` — история переводов/кредитов (вся или по пользователю).

---

## Что есть на фронте
- `/auth` — вход, вывод текущего сотрудника, хедер.
- `/accounts` — список счетов (для обычного USER — только свой), личный счёт в KZT.
- `/transfer` — перевод по username (поле отправителя подставляется из логина), подсказки получателей, история операций.
- `/loan` — оформление кредита на выбранный счёт.
- Фон с изображением `public/bank.webp`; светлая UI-схема с акцентами.

Навигация в TopBar показывает ссылки «Перевод» и «Кредит» только если роль допускает операции (USER).

---

## Ограничения и договорённости
- Пароли в открытом виде, токен mock без срока действия — не использовать в проде.
- Данные по счетам и истории — только в памяти; после рестарта бэкенда будут восстановлены заново.
- CORS открыт для всех доменов.
- Админ/менеджер/employee не могут выполнять переводы/кредиты (политика демо).

---

## Полезные команды
- Backend: `./mvnw spring-boot:run`, `./mvnw clean package`, `./mvnw test`.
- Frontend: `npm run dev`, `npm run lint`, `npm run build && npm run start`.
