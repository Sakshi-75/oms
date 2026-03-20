## Legacy Order Management System – Modernization Playground

This project is a **legacy-style, medium-sized monolithic Order Management System** built with:

- **Java 11**
- **Spring Boot 2.7.18**
- **Maven**
- **H2 in-memory database**
- **JaCoCo** (100% line + branch coverage enforced)
- **PIT** (100% mutation + coverage threshold)

It is intentionally designed to feel like a **realistic legacy codebase**, with classic patterns and some outdated idioms, so you can safely practice:

- Java syntax modernization
- Concurrency and async modernization
- HTTP client migration
- Context propagation improvements
- Performance/profiling
- Safe refactoring under full test + mutation coverage

---

## High-Level Architecture Diagram

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
      +---------+----------+                   |
      |  Orchestration &   |                   |
      |  Domain Services   |-------------------+
      |                    |
      | - InventoryService |
      | - ProductService   |
      | - OrderDashboard   |
      |   AggregationSvc   |
      | - NotificationDis- |
      |   patchService     |
      | - ReportGeneration |
      |   Service          |
      +----------+---------+
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

1. **HTTP request** hits a controller.
2. Controller delegates to **orchestrating services**.
3. Services talk to:
   - **Repositories** (H2 database via Spring Data JPA)
   - **NotificationDispatchService** (async via `ExecutorService`)
   - **ReportGenerationService** and **CsvExportUtil**
4. A **ThreadLocal correlation context** (`CorrelationIdHolder`) is attached per request via `RequestCorrelationFilter`.

---

## Project Structure

### Package-level view

```text
com.example.oms
├── OmsLegacyApplication            # Spring Boot main class
├── config                          # Legacy-style configuration & context
│   ├── CorrelationIdHolder         # ThreadLocal-based correlation/context
│   ├── RequestCorrelationFilter    # Servlet filter that sets/clears context
│   └── LegacyExecutorConfig       # Fixed thread pool for async work
├── controller                      # REST API controllers (HTTP layer)
│   ├── InventoryController
│   ├── NotificationDispatchController
│   ├── OrderDashboardController
│   ├── ProductController
│   └── ReportGenerationController
├── dto                             # Request/response DTOs (POJO style)
│   ├── AuditTimelineItemDto
│   ├── DailyOrdersReportResponseDto
│   ├── InventoryItemDto
│   ├── NotificationDispatchRequestDto
│   ├── NotificationDispatchResponseDto
│   ├── NotificationDispatchResultDto
│   ├── NotificationSummaryDto
│   ├── OrderDashboardDto
│   ├── OrderDetailsDto
│   ├── PaymentSummaryDto
│   ├── ProductDto
│   └── ShipmentSummaryDto
├── entity                          # JPA entities & enums
│   ├── AuditEventType
│   ├── AuditTrail
│   ├── Customer
│   ├── DailyOrdersReportGeneration
│   ├── InventoryItem
│   ├── InventoryStatus
│   ├── Notification
│   ├── NotificationType
│   ├── Order
│   ├── OrderItem
│   ├── OrderStatus
│   ├── Payment
│   ├── PaymentMethod
│   ├── PaymentStatus
│   ├── Product
│   ├── ProductStatus
│   ├── ReportGenerationStatus
│   ├── Shipment
│   └── ShipmentStatus
├── exception                       # Exceptions & global error handling
│   ├── ApiError
│   ├── BusinessException
│   ├── NotFoundException
│   └── GlobalExceptionHandler
├── mapper                          # Manual mappers (DTO <-> entity)
│   ├── InventoryItemMapper
│   ├── OrderDashboardMapper
│   └── ProductMapper
├── repository                      # Spring Data JPA repositories
│   ├── AuditTrailRepository
│   ├── CustomerRepository
│   ├── DailyOrdersReportGenerationRepository
│   ├── InventoryItemRepository
│   ├── NotificationRepository
│   ├── OrderRepository
│   ├── PaymentRepository
│   ├── ProductRepository
│   └── ShipmentRepository
├── service                         # Business logic / orchestration
│   ├── InventoryService
│   ├── NotificationDispatchService # ExecutorService-based async workflow
│   ├── OrderDashboardAggregationService
│   ├── ProductService
│   └── ReportGenerationService
└── util                            # Utilities & legacy formatting logic
    └── CsvExportUtil               # Manual CSV building
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
                      v
           +----------+-----------+
           |   Repositories       |
           |   (JPA)              |
           +----------+-----------+
                      |
                      v
           +----------+-----------+
           |   H2 Database        |
           +----------------------+
```

---

## Business Workflows (High-Level)

### Order Dashboard

- `OrderDashboardController` → `OrderDashboardAggregationService`
- Aggregates order details, payment summaries, shipment summaries, audit timelines, and notification summaries into a single `OrderDashboardDto`.

### Inventory Management

- `InventoryController` → `InventoryService` → `InventoryItemRepository`
- Manages inventory items with status transitions (`AVAILABLE`, `RESERVED`, `OUT_OF_STOCK`).

### Product Management

- `ProductController` → `ProductService` → `ProductRepository`
- CRUD operations on products with status tracking (`ProductStatus`).

### Notification Dispatch

- `NotificationDispatchController` → `NotificationDispatchService`
- Uses `ExecutorService` (from `LegacyExecutorConfig`) to send notifications asynchronously.

### Report Generation

- `ReportGenerationController` → `ReportGenerationService`
- Generates daily order reports with CSV export via `CsvExportUtil`.
- Tracks report generation status (`ReportGenerationStatus`).

---

## Running the Application

### Prerequisites

- Java 11 (JDK 11)
- Maven 3.8+

### Start the app

```bash
mvn clean spring-boot:run
```

The application runs at:

- `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:omsdb`
  - User: `sa`
  - Password: *(empty)*

---

## Testing, Coverage, and Mutation Testing

### Run tests with coverage

```bash
mvn clean verify
```

This runs:

- Unit tests (JUnit 5 + Mockito 5.12)
- **JaCoCo**: generates coverage report and **fails the build** if:
  - Line coverage < **100%**
  - Branch coverage < **100%**

JaCoCo report: `target/site/jacoco/index.html`

### Run mutation testing

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

- **PIT** targets all classes under `com.example.oms.*`
- **Fails** if mutation score or coverage threshold < **100%**
- Report: `target/pit-reports/index.html`

---

## Test Structure

```text
com.example.oms (test)
├── OmsLegacyApplicationTest
├── config/
│   ├── CorrelationIdHolderTest
│   ├── LegacyExecutorConfigTest
│   └── RequestCorrelationFilterTest
├── controller/
│   ├── InventoryControllerTest
│   ├── NotificationDispatchControllerTest
│   ├── OrderDashboardControllerTest
│   ├── ProductControllerTest
│   └── ReportGenerationControllerTest
├── dto/
│   └── DtoCoverageTest
├── entity/
│   ├── CustomerTest
│   └── OrderAndFinanceEntitiesTest
├── exception/
│   └── GlobalExceptionHandlerTest
├── mapper/
│   ├── InventoryItemMapperTest
│   ├── OrderDashboardMapperTest
│   └── ProductMapperTest
├── service/
│   ├── InventoryServiceTest
│   ├── NotificationDispatchServiceTest
│   ├── OrderDashboardAggregationServiceTest
│   ├── ProductServiceTest
│   └── ReportGenerationServiceTest
└── util/
    └── CsvExportUtilTest
```
