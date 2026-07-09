# Library System

A REST API for managing books, borrowers, and borrowing records in a library system.

Built with **Java 17** and **Spring Boot 4.1**, using **Gradle** for dependency management and **PostgreSQL** for persistence.

---

## Requirements Coverage

| # | Requirement | Implementation |
|---|-------------|----------------|
| 1 | Java 17 + Spring Boot | `build.gradle` uses JDK 17 toolchain, Spring Boot 4.1, `spring-boot-starter-web` |
| 2 | Configurable for multiple environments | Profile-based config: `application-dev.properties`, `application-prod.properties`, `application-test.properties` |
| 3 | Package manager | Gradle with `build.gradle`, `settings.gradle`, and Gradle wrapper |
| 4 | Data validation & error handling | Jakarta Bean Validation (`@NotBlank`, `@Email`) on request DTOs; `@RestControllerAdvice` global exception handler |
| 5 | Database for borrowers & books | PostgreSQL via JPA/Hibernate + Liquibase migrations |
| 6 | REST API endpoints | See **API Endpoints** section below |
| 7 | Same ISBN → different book IDs | `Book` (physical copy) has its own ID; `BookEdition` groups by ISBN/title/author. Each `POST /api/books` creates a new `Book` row |
| 8 | No concurrent borrow of same book copy | `books.available` boolean checked before borrow; DB-level partial unique index on `borrow_records(book_id) WHERE status = 'BORROWED'` |
| 9 | API documentation | This README + Swagger UI at `/swagger-ui.html` |
| 10 | Assumptions | See **Assumptions** section below |

---

## Database Choice: PostgreSQL

**PostgreSQL** was chosen for the following reasons:

- **ACID compliance** – ensures transactional integrity during borrow/return operations (critical when checking `available` flag and inserting `borrow_records` in the same transaction).
- **Partial unique indexes** – the constraint `CREATE UNIQUE INDEX ... ON borrow_records(book_id) WHERE status = 'BORROWED'` is a PostgreSQL-specific feature that guarantees, at the database level, that no book can be borrowed by two members simultaneously. This is difficult to enforce portably with standard SQL constraints.
- **Maturity and ecosystem** – well-supported by Spring Boot, Hibernate, Liquibase, and production-grade hosting.
- **JSONB support** – available for future extensibility (e.g., storing borrower metadata, book reviews).
- **Open source** – no licensing costs.

---

## Prerequisites

- JDK 17
- Docker (for PostgreSQL via docker-compose) or a local PostgreSQL 16 instance
- Gradle 8.14+ (the wrapper `gradlew` is included)

---

## Quick Start

### Option A: Run everything with Docker Compose (recommended)

Builds the app and starts both services (PostgreSQL + Spring Boot) with a single command:

```bash
docker compose -f docker/docker-compose.yml up --build
```

The app starts on `http://localhost:8080` with the `dev` profile.

### Option B: Run app locally with Docker PostgreSQL

#### 1. Start PostgreSQL

```bash
docker compose -f docker/docker-compose.yml up -d db
```

#### 2. Run the application

```bash
.\gradlew.bat bootRun
```

The app starts on `http://localhost:8080` with the `dev` profile by default.

### Run tests

```bash
.\gradlew.bat test
```

Tests use an in-memory H2 database and do not require a running PostgreSQL instance.

### Swagger UI

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to explore and test endpoints interactively.

API docs (OpenAPI JSON): [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## Environment Configuration

| Profile   | File                           | Database          | Usage              |
|-----------|--------------------------------|-------------------|--------------------|
| `dev`     | `application-dev.properties`   | PostgreSQL local  | Local development  |
| `prod`    | `application-prod.properties`  | PostgreSQL remote | Production         |
| `test`    | `application-test.properties`  | H2 in-memory      | Automated tests    |

Activate a profile via `SPRING_PROFILES_ACTIVE` environment variable:

```bash
SPRING_PROFILES_ACTIVE=prod .\gradlew.bat bootRun
```

---

## API Endpoints

### Books

#### `POST /api/books` – Register a new book copy

Registers a new physical copy of a book. If the ISBN/title/author combination already exists, a new copy is still created (the edition is reused). If the ISBN exists but with different title/author, the request is rejected.

**Request body:**

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0-13-235088-4"
}
```

**Response `201 Created`:**

```json
{
  "id": 1,
  "isbn": "978-0-13-235088-4",
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "available": true
}
```

**Validation errors `400 Bad Request`:**

```json
{
  "title": "Title is required",
  "isbn": "ISBN is required"
}
```

**ISBN mismatch `400 Bad Request`:**

```json
{
  "error": "ISBN 978-0-13-235088-4 already exists with title 'Clean Code' and author 'Robert C. Martin'"
}
```

#### `GET /api/books` – List all books (paginated)

**Query parameters:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page`    | `0`     | Page index (0-based) |
| `size`    | `20`    | Items per page |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": 1,
      "isbn": "978-0-13-235088-4",
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "available": true
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Borrowers

#### `POST /api/borrowers` – Register a borrower

**Request body:**

```json
{
  "name": "Alice Smith",
  "email": "alice@example.com"
}
```

**Response `201 Created`:**

```json
{
  "id": 1,
  "name": "Alice Smith",
  "email": "alice@example.com"
}
```

#### `POST /api/borrowers/{borrowerId}/borrow/{bookId}` – Borrow a book

**Response `200 OK`:**

```
Book borrowed successfully
```

**If the book is already borrowed `409 Conflict`:**

```json
{
  "error": "Book is already borrowed: 1"
}
```

#### `POST /api/borrowers/{borrowerId}/return/{bookId}` – Return a book

**Response `200 OK`:**

```
Book returned successfully
```

**If the borrower did not borrow this book `409 Conflict`:**

```json
{
  "error": "Borrower did not borrow this book: 1"
}
```

---

## Error Handling

All errors return a JSON body with either a string `error` field or a map of field-level validation errors.

| HTTP Status | When |
|-------------|------|
| `400 Bad Request` | Validation failure or `IllegalArgumentException` (e.g., ISBN mismatch, entity not found) |
| `409 Conflict` | `IllegalStateException` (e.g., book already borrowed, borrower did not borrow this book) |
| `500 Internal Server Error` | Unexpected exceptions (stack trace logged, generic message returned) |

---

---

## CI/CD

The repository includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that runs on every push and pull request to the `main` branch. It compiles the project, runs tests, and packages the application.

A **deploy** step is defined as a placeholder only — actual deployment is out of scope for this task. To enable deployment, replace the placeholder script with your target platform's deployment command.

---

## Assumptions

1. **Email not required to be unique** – Multiple borrowers may share the same email address. No uniqueness constraint is enforced on `borrowers.email`.

2. **ISBN uniqueness per edition** – A given ISBN maps to exactly one title/author combination. If the same ISBN is registered with different metadata, it is treated as an error (data integrity concern). If a new edition of a book has a different title/author, it should use a different ISBN per industry standards.

3. **No due dates or overdue tracking** – The system tracks borrow/return timestamps but does not enforce borrowing periods or calculate fines. This is outside the scope of the stated requirements.

4. **Soft return only** – Returning a book does not delete the `BorrowRecord`; it updates `status` to `RETURNED` and sets `returned_at`. The full borrowing history is preserved.

5. **Partial unique index over application-level check** – The `borrow_records` table has a PostgreSQL partial unique index `(book_id) WHERE status = 'BORROWED'`. This acts as a database-level circuit breaker in addition to the `available` boolean check. If two concurrent requests pass the `available` check before either commits, the unique index violation will cause one to roll back.

6. **No authentication/authorization** – The API is open. Adding security (e.g., Spring Security, JWT) would be required for a production deployment but is not part of these requirements.

7. **20 items per page default** – When listing books, the default page size is 20. Clients can override via the `size` parameter.

8. **Timestamp precision** – All timestamps use `ZonedDateTime` (TIMESTAMPTZ in PostgreSQL) to handle timezone-aware date/time recording.

9. **Sorting and searching by `isbn`, `title`, or `author` is not required, and the dataset is assumed small enough** that a `LEFT JOIN` to `book_editions` is not a performance concern. If these assumptions change, the columns can be duplicated to `books` without changing the API contract.
