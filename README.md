# Finance Dashboard Backend

A Java Spring Boot backend for a finance dashboard system with role-based access control, financial record management, and analytics APIs.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Database | PostgreSQL (production) / H2 (development) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (optional — H2 runs by default)

### Run with H2 (zero setup)
```bash
mvn spring-boot:run
```
App starts at `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console`.

### Run with PostgreSQL
```bash
# Create database
createdb finance_db

# Run with postgres profile
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```
Edit `src/main/resources/application-postgres.properties` for custom DB credentials.

### Seed Data
On first startup, the app auto-seeds:
- **3 users**: `admin/admin123` (ADMIN), `analyst/analyst123` (ANALYST), `viewer/viewer123` (VIEWER)
- **12 sample financial records** across 3 months

## Architecture

```
com.finance/
├── config/          # Security config, data seeder
├── security/        # JWT utility, filter, UserDetailsService
├── model/           # JPA entities (User, FinancialRecord, Role enum)
├── dto/             # Request/Response DTOs
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic layer
├── controller/      # REST API endpoints
└── exception/       # Global exception handler, custom exceptions
```

**Design decisions:**
- **3-layer architecture** (Controller → Service → Repository) with clear separation of concerns
- **DTOs** for all API input/output — entities are never exposed directly
- **Soft delete** on financial records (records are flagged, not removed)
- **JWT stateless auth** — no server-side sessions
- **H2 default profile** for instant dev setup; PostgreSQL for production

## API Reference

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login, get JWT token | Public |

**Login request:**
```json
{ "username": "admin", "password": "admin123" }
```
**Response:**
```json
{ "token": "eyJhbG...", "username": "admin", "role": "ADMIN" }
```

Use the token in all subsequent requests:
```
Authorization: Bearer <token>
```

### Users (ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create user |
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| PATCH | `/api/users/{id}/role` | Update role (`{"role": "ANALYST"}`) |
| PATCH | `/api/users/{id}/status` | Toggle active/inactive |

### Financial Records

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/records` | VIEWER, ANALYST, ADMIN | List records (paginated, filterable) |
| GET | `/api/records/{id}` | VIEWER, ANALYST, ADMIN | Get single record |
| POST | `/api/records` | ADMIN | Create record |
| PUT | `/api/records/{id}` | ADMIN | Update record |
| DELETE | `/api/records/{id}` | ADMIN | Soft delete record |

**Filter parameters** (all optional):
```
GET /api/records?type=INCOME&category=Salary&startDate=2026-01-01&endDate=2026-03-31&page=0&size=20
```

**Create/update body:**
```json
{
  "amount": 50000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-03-01",
  "description": "March salary"
}
```

### Dashboard (ANALYST, ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/summary` | Full dashboard summary |

**Response includes:**
- `totalIncome`, `totalExpenses`, `netBalance`
- `totalRecords` count
- `incomeByCategory`, `expenseByCategory` (maps)
- `recentActivity` (last 10 records)
- `monthlyTrends` (last 6 months: income, expense, net per month)

## Access Control Matrix

| Action | VIEWER | ANALYST | ADMIN |
|--------|--------|---------|-------|
| View records | Yes | Yes | Yes |
| View dashboard | No | Yes | Yes |
| Create/edit/delete records | No | No | Yes |
| Manage users | No | No | Yes |

Enforced at two levels:
1. **URL-level** via `SecurityConfig` (HttpSecurity rules)
2. **Method-level** via `@PreAuthorize` annotations

## Validation & Error Handling

All inputs are validated with Jakarta Bean Validation (`@NotBlank`, `@Email`, `@DecimalMin`, etc.). Errors return structured JSON:

```json
{
  "timestamp": "2026-03-26T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "fields": {
    "amount": "Amount must be positive",
    "category": "Category is required"
  }
}
```

HTTP status codes used: `200`, `201`, `400`, `401`, `403`, `404`, `409`, `500`.

## Assumptions & Tradeoffs

1. **H2 as default** — chose developer ergonomics over production readiness. PostgreSQL is one flag away (`--spring.profiles.active=postgres`).
2. **Soft delete** — financial records should never be permanently removed for audit reasons. The `deleted` flag filters them from all queries.
3. **Monthly trends via SQL** — aggregation is done in the database, not in Java. This is efficient but the `TO_CHAR` function is PostgreSQL-specific; H2 may return slightly different month formatting.
4. **Single JWT secret** — fine for a demo; production would use RSA key pairs or a secrets manager.
5. **No refresh tokens** — tokens expire in 24h. A production system would implement refresh token rotation.
6. **Registration is public** — in a real system, only admins would create users. The public `/register` endpoint exists for easy testing.

## Optional Enhancements Included

- [x] JWT authentication
- [x] Pagination on record listing
- [x] Soft delete
- [x] Data seeding for instant testing
- [x] Structured error responses
- [x] Input validation with field-level errors
- [x] Database indexing on frequently queried columns

## Author

Riya Mittal — riya.mittal@zop.dev
