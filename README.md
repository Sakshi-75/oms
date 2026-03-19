## Legacy Order Management System – Modernization Playground

This project is a **legacy-style, medium-sized monolithic Order Management System** built with:

- **Java 11**
- **Spring Boot 2.7**
- **Maven**
- **H2 in-memory database**
- **JaCoCo + PIT** for strict test and mutation coverage

It is intentionally designed to feel like a **realistic legacy codebase**, with classic patterns and some outdated idioms, so you can safely practice:

- Java syntax modernization
- Concurrency and async modernization
- HTTP client migration
- Context propagation improvements
- Performance/profiling
- Safe refactoring under full test + mutation coverage

---

## High-Level “Sticky” Diagram

Think of the system as a set of sticky notes on a whiteboard, showing the major domains and flows.

```text
     +-------------------+          +--------------------+
     |   Auth / Context  |          |  External Systems  |
     |  (ThreadLocal)    |          |                    |
     +----------+--------+          |  Payment Gateway   |
                |                   |  Shipping Service  |
                v                   |  Notification APIs |
      +---------+---------+         +---------+----------+
      |   HTTP Controllers |                   ^
      | (REST endpoints)   |                   |
      +---------+----------+                   |
                |                              |
                v                              |
      +---------+----------+          +--------+---------+
      |  Orchestration &   |          | External Clients |
      |  Domain Services    |<--------| (RestTemplate)   |
      |                     |          +-----------------+
      | - CustomerService   |
      | - OrderService      |
      | - PaymentService    |
      | - ProductService    |
      | - InventoryService  |
      | - PricingService    |
      | - ShippingService   |
      | - NotificationSvc   |
      | - ReportService     |
      | - AuditService      |
      +----------+----------+
                 |
                 v
      +----------+----------+
      |   Repositories      |
      | (Spring Data JPA)   |
      +----------+----------+
                 |
                 v
      +----------+----------+
      |   H2 Database       |
      +---------------------+
```

Core end-to-end workflow (simplified):

1. **HTTP request** hits controller.
2. Controller delegates to **orchestrating services**.
3. Services talk to:
   - **Repositories** (H2 database)
   - **External clients** (payment, shipping) via `RestTemplate`
   - **NotificationService** (async via `ExecutorService`)
   - **AuditService** and **ReportService**
4. A **ThreadLocal correlation / auth context** is attached per request for logging/audit.

---

## Project Structure Diagram

### Package-level view

```text
com.example.oms
├── OmsLegacyApplication        # Spring Boot main class
├── config                      # Legacy-style configuration & context
│   ├── CorrelationIdHolder     # ThreadLocal-based correlation/context
│   ├── RequestCorrelationFilter# Servlet filter that sets/clears context
│   ├── ExecutorConfig          # Fixed thread pool for async work
│   └── RestTemplateConfig      # Legacy RestTemplate bean
├── controller                  # REST API controllers (HTTP layer)
│   ├── CustomerController
│   ├── OrderController
│   ├── PaymentController
│   ├── ProductController
│   ├── InventoryController
│   ├── NotificationController
│   └── ReportController
├── dto                         # Request/response DTOs (POJO style)
│   ├── CustomerDto
│   ├── OrderDto
│   ├── OrderItemDto
│   ├── PaymentDto
│   ├── NotificationDto
│   ├── ProductDto
│   ├── InventoryItemDto
│   ├── CustomerOrderSummaryDto
│   ├── PaymentSummaryDto
│   └── DailyOrderReportDto
├── entity                      # JPA entities & legacy enums
│   ├── Customer
│   ├── Order
│   ├── OrderItem
│   ├── Payment
│   ├── Notification
│   ├── Product
│   ├── InventoryItem
│   ├── OrderStatus
│   ├── PaymentStatus
│   ├── PaymentMethod
│   ├── NotificationType
│   ├── ProductStatus
│   └── InventoryStatus
├── exception                   # Exceptions & global error handling
│   ├── ApiError
│   ├── BusinessException
│   ├── NotFoundException
│   └── GlobalExceptionHandler
├── mapper                      # Manual mappers (DTO <-> entity)
│   ├── CustomerMapper
│   ├── OrderMapper
│   ├── PaymentMapper
│   ├── NotificationMapper
│   ├── ProductMapper
│   └── InventoryItemMapper
├── repository                  # Spring Data JPA repositories
│   ├── CustomerRepository
│   ├── OrderRepository
│   ├── PaymentRepository
│   ├── NotificationRepository
│   ├── ProductRepository
│   └── InventoryItemRepository
├── service                     # Business logic / orchestration
│   ├── CustomerService
│   ├── OrderService
│   ├── PaymentService
│   ├── ProductService
│   ├── InventoryService
│   ├── NotificationService     # ExecutorService-based async workflow
│   └── ReportService           # Reporting & CSV preparation
├── integration                 # Outgoing HTTP integrations (legacy style)
│   └── PaymentGatewayClient    # RestTemplate-based payment client
├── util                        # Utilities & legacy formatting logic
│   ├── CsvExportUtil           # Manual CSV building
│   └── ReportFormatter         # StringBuilder + switch/if-else formatting
└── bootstrap                   # Startup data
    └── DataInitializer         # Seed data on application start
```

### Layered architecture diagram

```text
           +----------------------+
           |   Controllers        |
           | (REST APIs)          |
           +----------+-----------+
                      |
                      v
           +----------+-----------+
           | Services & Workflow  |
           | - Domain rules       |
           | - Orchestration      |
           +----------+-----------+
                      |
      +---------------+---------------+
      |                               |
      v                               v
+-----+------+               +--------+--------+
|  Repositories|             | External Clients|
| (JPA)        |             | (RestTemplate)  |
+-----+--------+             +--------+--------+
      |                               |
      v                               v
+-----+--------+              +-------+--------+
|   H2 DB      |              | Payment / Ship |
+--------------+              +----------------+
```

---

## Business Workflows (High-Level)

The goal is to support realistic, multi-step workflows that can be safely modernized later.

### Customer / Order / Payment / Shipping flow (happy path)

1. **Create customer**
   - `CustomerController` → `CustomerService` → `CustomerRepository`
2. **Create order with multiple items**
   - `OrderController` → `OrderService`
   - Validates customer existence via `CustomerService`
   - Computes totals via collection loops (legacy-style)
3. **Validate product availability**
   - `OrderService` / future `OrderWorkflowService` interacts with `ProductService` and `InventoryService`
4. **Reserve inventory**
   - `InventoryService.reserve` with nested `if`/`else` rules and status updates (`AVAILABLE`, `RESERVED`, `OUT_OF_STOCK`)
5. **Calculate price with rules/discounts/taxes**
   - Current version uses simpler totals; `ReportService` and DTOs are prepared for richer pricing logic.
6. **Initiate payment via external payment client**
   - `PaymentService` → `PaymentGatewayClient` (RestTemplate)
7. **Handle payment success/failure/retry**
   - `PaymentService.retryFailed` uses multiple branches to update `PaymentStatus`
8. **Create shipment via external shipping client**
   - (Hook point for a future `ShippingClient` / `ShippingService` using `RestTemplate` or newer HTTP clients)
9. **Send notifications**
   - `NotificationService` uses `ExecutorService` to send messages asynchronously
   - Uses `instanceof` + casting on payload, and manual string formatting
10. **Record audit trail**
    - (Hook point for a future `AuditService` writing to DB or log)
11. **Generate summary and CSV reports**
    - `ReportService` + `ReportFormatter` + `CsvExportUtil`

As you expand the project (e.g., adding explicit pricing, shipping, and audit modules), you can plug into this structure while keeping the legacy characteristics.

---

## Running the Application

### Prerequisites

- Java 11 (JDK 11)
- Maven 3.8+ installed and on your `PATH`

### Start the app

```bash
mvn clean spring-boot:run
```

The application runs at:

- `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:omsdb`
  - User: `sa`
  - Password: (empty)

Sample seed data is loaded on startup by `DataInitializer`.

---

## Testing, Coverage, and Mutation Testing

### Run tests with coverage

```bash
mvn clean verify
```

This runs:

- Unit tests and integration-style tests
- **JaCoCo**: generates coverage report and fails the build if:
  - Line coverage < 100%
  - Branch coverage < 100%

JaCoCo report:

- Open `target/site/jacoco/index.html` in a browser.

### Mutation testing with PIT

PIT is configured in `pom.xml` to treat **100% mutation score** as required:

```bash
mvn clean test org.pitest:pitest-maven:mutationCoverage
```

Open the PIT report:

- `target/pit-reports/index.html`

If any mutants survive, the build will fail. This is designed to force tests that are strong enough to support aggressive refactoring and modernization.

---

## Intentional Legacy Patterns (for Modernization Practice)

The codebase is intentionally **not “perfect”**: it includes older patterns distributed across modules so you can practice modernizing them.

- **Traditional DTO POJOs**
  - All DTOs under `com.example.oms.dto` use mutable fields, constructors, getters, and setters.
  - Candidate for conversion to **records** in later Java versions.
- **Old-style switch statements & if-else chains**
  - `ReportFormatter` uses classic switches and nested if-else logic.
  - `PaymentService` and `InventoryService` have branching based on enums and numeric rules.
  - These are ideal for refactoring to **switch expressions** and **pattern matching** later.
- **`instanceof` + manual casting**
  - `NotificationService` handles payloads with `instanceof` checks and casts.
  - Good candidate to migrate to **pattern matching for instanceof**.
- **ExecutorService-based async processing**
  - `NotificationService` uses a fixed thread pool from `ExecutorConfig`.
  - Great for exploring migration to **virtual threads** or structured concurrency.
- **RestTemplate-based external integrations**
  - `PaymentGatewayClient` uses `RestTemplate` with manual `postForEntity` and status checks.
  - Ideal for migration to **Java HttpClient** or **WebClient**.
- **ThreadLocal-based request context**
  - `CorrelationIdHolder` + `RequestCorrelationFilter` use `ThreadLocal` to propagate a correlation id.
  - These can be replaced with a more modern context propagation approach.
- **Manual string formatting**
  - `ReportFormatter` and `NotificationService` construct messages with `StringBuilder` and concatenation.
  - Good for refactoring to text blocks, `String::formatted`, or template engines.
- **Imperative collection manipulation**
  - Services and report generation use explicit loops and mutable accumulators.
  - Natural candidate for refactoring to **streams** and expressive collectors.
- **Enums and status flags**
  - `OrderStatus`, `PaymentStatus`, `PaymentMethod`, `NotificationType`, `ProductStatus`, `InventoryStatus`.
  - Can evolve into **sealed hierarchies** with richer behavior.

---

## Planned Modernization Opportunities

Some concrete modernization experiments you can perform:

- **DTOs → Records**
  - Convert DTOs to `record`s and adjust mappers & JSON serialization.
- **Enums → Sealed hierarchies**
  - Replace enums with sealed interfaces/classes for order/payment/inventory states.
- **Legacy flow control → Modern constructs**
  - Refactor `ReportFormatter` and service branch logic using:
    - switch expressions
    - pattern matching for `instanceof`
- **Concurrency modernization**
  - Replace `ExecutorService` in `ExecutorConfig` / `NotificationService` with:
    - virtual threads
    - structured concurrency or modern Spring async
- **HTTP client migration**
  - Migrate `PaymentGatewayClient` from `RestTemplate` to:
    - Java 11+ `HttpClient`
    - or `WebClient` (Spring WebFlux)
- **Context propagation**
  - Replace `ThreadLocal` correlation/context with a safer approach that works with async and virtual threads.
- **Reporting and CSV improvements**
  - Replace `CsvExportUtil` and `ReportFormatter` with:
    - robust CSV libraries
    - UTF-8 handling and localization-aware formatting
- **Collection and stream refactors**
  - Convert imperative loops in `OrderService`, `ReportService`, and others to streams.
- **JDK upgrades**
  - Step-by-step upgrades from Java 11 → 17 → 21 → 25, enabling newer language features and JVM improvements at each step.

This README is meant to serve as a **map of the monolith** and a **checklist of modernization targets** as you work through multi-week refactoring and migration exercises.

