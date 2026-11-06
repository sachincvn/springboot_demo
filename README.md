# Spring Demo Store API

Learning project: a small store backend (products, orders, payments) built the way production Spring teams structure code.

## Run

1. Copy `.env.example` to `.env` (git-ignored) and set your DB password.
2. `./mvnw spring-boot:run` (runs the `dev` profile by default)
3. Swagger UI: http://localhost:8080/swagger-ui.html

Tests: `./mvnw test` (`SpringdemoApplicationTests` needs the database; the others don't).

## Environments

One JAR, three profiles. Select with `SPRING_PROFILES_ACTIVE=dev|qa|prd` (IntelliJ: Run config → Active profiles).

| | dev (laptop) | qa (test server) | prd (production) |
|---|---|---|---|
| Config file | `application-dev.yaml` | `application-qa.yaml` | `application-prd.yaml` |
| DB credentials | `.env` file | env vars from CI/CD | env vars from secrets manager |
| Schema | Hibernate `update` | `validate` only | `validate` only |
| SQL logging | on | off | off |
| Log level (app) | DEBUG | DEBUG | INFO (root WARN) |
| Swagger | on | on | off |
| DB pool size | default (10) | 5 | 20 |

`application.yaml` holds the shared defaults; each profile file contains only what differs.
Secrets are never in YAML, only `${DB_PASSWORD}`-style placeholders.

### Database per environment

Each environment has its **own database server and credentials**. The code is identical; only the
environment variables differ, set where that environment runs:

| | `DB_URL` (example) | Set by |
|---|---|---|
| dev | `jdbc:postgresql://localhost:5432/springdemo_db` | your `.env` |
| qa | `jdbc:postgresql://qa-db.internal:5432/springdemo_qa` | CI/CD secrets for the qa deploy |
| prd | `jdbc:postgresql://prd-db.internal:5432/springdemo` | secrets manager for production |

Rules: never point dev or qa at the production database, and never reuse prd credentials elsewhere.

## Branching (Git flow-lite)

| Branch | Deploys to | Who merges |
|---|---|---|
| `feature/*`, `bugfix/*` | nowhere (your laptop) | you |
| `develop` | dev environment | PR from feature branch, 1 review |
| `qa` | qa environment | PR `develop` → `qa` when a set of features is ready for testing |
| `main` | production (prd) | PR `qa` → `main` after QA sign-off, tagged `v1.2.0` |
| `hotfix/*` | prd, urgent fixes | branch from `main`, PR to `main`, then merge back into `qa` and `develop` |

Day to day:
```bash
git checkout develop && git pull
git checkout -b feature/order-search      # one branch per task
# ...commit work...
git push -u origin feature/order-search   # open a PR into develop
```
- Never commit directly to `develop`, `qa` or `main`; always through a pull request.
- The same commit flows upward (develop → qa → main); you never build a different version per env.
- Commit messages: `feat: ...`, `fix: ...`, `refactor: ...`, `docs: ...` (Conventional Commits).

## Structure: package by feature

```
common/        shared building blocks, no business logic
  entity/        BaseEntity (id, createdAt, updatedAt) all entities extend
  exception/     custom exceptions + GlobalExceptionHandler (one place for error JSON)
  mapper/        ResponseMapper<E, R> generic contract
  config/        Swagger, paging
  currency/      CurrencyCode enum
product/       catalogue + stock, price quotes in other currencies
order/         order lifecycle (state machine in OrderStatus), publishes OrderCancelledEvent
payment/       pays orders via pluggable gateways, refunds on cancellation
  gateway/       PaymentGateway strategy + sandbox implementations
integration/   adapters for third-party APIs
  exchangerate/  ExchangeRateClient port + Frankfurter adapter
```

Each feature has the same layers, and requests flow in one direction:

```
Controller  ->  Service (interface)  ->  ServiceImpl  ->  Repository  ->  DB
   DTOs in/out       business use case       entities hold the rules
```

| Layer | Job | Must NOT |
|---|---|---|
| Controller | HTTP: validate input (`@Valid`), status codes, Swagger docs | contain business logic or touch repositories |
| Service | one use case per method, transactions, orchestration | build HTTP responses |
| Entity | business rules (`decreaseStock`, `changeStatus`) | have public setters that bypass rules |
| Mapper | entity <-> DTO | contain logic |
| Repository | queries | contain logic |

## Where each principle lives

- **S**ingle responsibility: controller / service / mapper / repository each do one job.
- **O**pen/closed: new payment method = new `PaymentGateway` class, no edits to `PaymentServiceImpl`. New reaction to a cancelled order = new `@EventListener`, no edits to `OrderServiceImpl`.
- **L**iskov: any `PaymentGateway` / `ExchangeRateClient` implementation can replace another.
- **I**nterface segregation: small, focused interfaces (`ExchangeRateClient` has one method).
- **D**ependency inversion: services depend on interfaces (`ProductService`, `ExchangeRateClient`, `PaymentGateway`), not on concrete classes or vendors.

Patterns used: Strategy (`PaymentGateway`), Template Method (`SandboxPaymentGateway`), Adapter/Port (`ExchangeRateClient`), State machine (`OrderStatus`), Observer/events (`OrderCancelledEvent`), rich domain model (`Order`, `Product`).

## Error format

Every error is RFC 9457 `application/problem+json`:

```json
{ "status": 409, "title": "Business rule violated",
  "detail": "Insufficient stock for 'Keyboard': requested 50, available 5", "instance": "/api/v1/orders" }
```

| Exception | HTTP |
|---|---|
| validation failure | 400 (+ `errors` field map) |
| `ResourceNotFoundException` | 404 |
| `BusinessException`, optimistic lock, FK conflict | 409 |
| `ExternalServiceException` | 502 |
| anything else | 500 (message hidden, logged) |

## Not done yet (typical next steps)

- Flyway migrations instead of `ddl-auto: update`
- Spring profiles (`application-dev.yaml`, `application-prod.yaml`)
- Spring Security + JWT
- Caching exchange rates, retry/circuit breaker (Resilience4j)
- Testcontainers for real-DB integration tests, Docker Compose, CI pipeline
