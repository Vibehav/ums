# User Management Service — Backend

A production-ready REST API for managing user records (KYC-style profile data) built with Spring Boot 3.5.15 and Java 21, backed by MySQL 8.4.


## Documentation
- [Frontend Documentation](./frontend/Readme.md)
## Tech Stack

- Java 21
- Spring Boot 3.5.15
- Spring Data JPA + Hibernate
- MySQL 8.4
- Flyway (schema migrations)
- HikariCP (connection pooling)
- Lombok
- springdoc-openapi 2.6.0 (Swagger UI)
- Testcontainers + JUnit 5 (unit testing)

---

## Environment Variables

Set these before running the app. The app will fail to start if they are missing.

| Variable | Example Value | What it's for |
|---|---|---|
| `DB_USERNAME` | `user_management_app` | MySQL app user |
| `DB_PASSWORD` | `changeme` | MySQL app user password |
| `DB_ROOT_PASSWORD` | `rootchangeme` | MySQL root password (Docker only) |
| `MYSQL_DATABASE` | `user_management` | Database name |

---

## Local Setup

### Step 1 — Start MySQL via Docker

```bash
docker compose up -d
```

### Step 2 — Run the application

```bash
./mvnw spring-boot:run
```

On first startup, Flyway automatically creates all tables.

### Step 3 — Verify

Open Swagger UI in your browser:

```
http://localhost:8080/swagger-ui.html
```

If Swagger loads — everything is connected correctly.

---

## Configuration Reference

### HikariCP

| Setting | Value | Why |
|---|---|---|
| `maximum-pool-size` | 10 | Max simultaneous DB connections |
| `minimum-idle` | 2 | Connections kept open during quiet periods |
| `connection-timeout` | 30000 ms | How long to wait for a free connection before failing |
| `idle-timeout` | 600000 ms | Close unused connections after 10 minutes |

---

## API Reference

**Base URL:** `http://localhost:8080/api/v1`

| Method | Path | Description |
|---|---|---|
| `POST` | `/users` | Create a new user |
| `GET` | `/users` | List all active users (paginated) |
| `GET` | `/users/{id}` | Get a single user by ID |
| `PATCH` | `/users/{id}` | Partially update a user |
| `DELETE` | `/users/{id}` | Soft delete a user |
| `PATCH` | `/users/{id}/restore` | Restore a soft-deleted user |
| `GET` | `/users/deleted` | List all soft-deleted users |

---

## Running Tests

```bash
# Run all tests
./mvnw test
```

Unit tests use Mockito to mock all dependencies — no Spring context starts, no database, runs in milliseconds.

Controller tests use `@WebMvcTest` — loads only the web layer with the service mocked, verifies HTTP behaviour without touching the database.

---

## Pain Points and Learnings

- **MySQL charset defaulted to latin1**

`Problem:` The container started with *latin1* for client connections despite the server being `utf8mb4`. This silently corrupts non-ASCII characters. 

`Solution:` Fixed by adding `characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci` to the JDBC URL and `--character-set-server=utf8mb4` to the Docker command block.


- **Timezone needs setting in four places, not one**

`Problem:` While Setting `serverTimezone` in the JDBC URL alone I noticed it was not enough to set it at just one place. Hibernate and Jackson each handle timestamps independently and default to UTC. 

`Fix:` All four layers — JDBC, Hibernate, Jackson, and the Docker container — must be set to the same timezone.

- **Unique Constraint Blind Spot After Soft Delete**

`Problem:` If a soft-deleted user's Aadhaar or PAN was reused during a new registration, the old code missed it. It only checked active records using existsBy...(a db should have only one unique aadhaar,pan)
so a brand new email with a previously-used Aadhaar would slip through and save a duplicate government ID to the database.

`fix:` So the fix was replacing all six *existsBy...* boolean methods with *findBy...* object-fetching methods. A boolean can only say "conflict exists" — it can't say why. Fetching the actual object lets us check deletedAt and return two completely different messages to the client: "this field is already in use" (active) vs "account is inactive, please recover it" (soft-deleted).

- **Trade-off we made duing soft-delete**

The trade-off is marginal findBy fetches the whole row vs existsBy which only checks existence. For normal traffic this is irrelevant. At high scale the fix is Partial Fetching.

`Solution:` Projections: Define a tiny interface with only the columns you need (id, deletedAt), update the repository to return it, and service logic stays identical.

- **AADHAAR , PAN MASKING**

Every API response was returning the full Aadhaar number and PAN directly. It may raise security risk. So the fix is store full value stays in the database. The masked value is what every response returns. No special endpoint, no toggle, no "reveal" option — the full value never comes back through the general API. 
API now returns XXXXXXXX2346.
