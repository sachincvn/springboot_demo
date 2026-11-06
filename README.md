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
