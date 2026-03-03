# Observable Microservices E-Commerce Backend

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

A production-quality microservices e-commerce backend built with Spring Boot 3, Java 17, and PostgreSQL. Implements a full checkout saga across seven independently deployable services, all accessible through a central Spring Cloud Gateway.

---

## Table of Contents

- [System Architecture](#system-architecture)
- [Services Overview](#services-overview)
- [Key Design Decisions](#key-design-decisions)
- [API Reference](#api-reference)
- [Security Architecture](#security-architecture)
- [Database Schema](#database-schema)
- [Tech Stack](#tech-stack)
- [Running Locally](#running-locally)
- [Production Considerations](#production-considerations)

---

## System Architecture

```
                          +----------------------+
         Client Requests  |     API Gateway      |  :8080
         ───────────────► |  Spring Cloud Gateway|
                          |  JWT Auth Filter     |
                          +----------+-----------+
                                     | Routes by path prefix
              +──────────────────────+────────────────────────+
              |                      |                        |
              v                      v                        v
     +─────────────────+   +──────────────────+   +─────────────────────+
     |  User Service   |   | Product Service  |   |    Cart Service     |
     |  :8081          |   |  :8082           |   |    :8083            |
     |  Register       |   |  Catalog         |   |    Add / Remove     |
     |  Login (JWT)    |   |  Categories      |   |    Qty Update       |
     |  Profiles       |   |  Inventory       |   |    Auto-total       |
     +─────────────────+   +──────────────────+   +─────────────────────+
           |                        ^                      |
           | own DB                 | WebClient            | WebClient
           v                        |                      v
      [user_db]            +────────────────────────────────────────+
                           |         Order Service  :8084           |
                           |  Checkout saga orchestrator            |
                           |  Calls: Product -> Payment -> Notify   |
                           +────────────+───────────────────────────+
                                        |
              +─────────────────────────+──────────────────────────+
              |                                                     |
              v                                                     v
  +──────────────────────+                     +───────────────────────────+
  |   Payment Service    |                     |   Notification Service    |
  |   :8085              |                     |   :8086                   |
  |   Mock charge        |                     |   Mock Email / SMS        |
  |   Refund             |                     |   Audit log of sends      |
  |   90% success rate   |                     |                           |
  +──────────────────────+                     +───────────────────────────+
```

---

## Services Overview

| Service              | Port | Database         | Responsibility                                  |
|----------------------|------|------------------|-------------------------------------------------|
| API Gateway          | 8080 | —                | Route all requests, JWT validation, CORS        |
| User Service         | 8081 | user_db          | Register, Login (JWT), Profile CRUD             |
| Product Service      | 8082 | product_db       | Catalog, Categories, Inventory management       |
| Cart Service         | 8083 | cart_db          | Per-user cart, denormalised product snapshots   |
| Order Service        | 8084 | order_db         | Checkout saga (product -> payment -> notify)    |
| Payment Service      | 8085 | payment_db       | Mock payment processing, refunds                |
| Notification Service | 8086 | notification_db  | Mock Email/SMS dispatch and audit log           |

---

## Key Design Decisions

### 1. JWT Validation at the Gateway

JWT tokens are validated once at the API Gateway. The gateway injects `X-User-Email` and `X-Auth-Token` headers into downstream requests. Downstream services never handle raw JWTs — they rely solely on the trusted headers set by the gateway.

### 2. Database-per-Service

Each service owns its schema exclusively. There are no cross-service JOINs. This enforces bounded contexts and allows independent schema evolution and scaling per service.

### 3. Denormalised Snapshots in Cart and Order Items

Cart items and order items capture a snapshot of product data (name, SKU, price) at the time of add or checkout. This preserves historical accuracy — a subsequent price change on a product does not alter past carts or orders.

### 4. Checkout Saga (Synchronous Orchestration)

The Order Service orchestrates a synchronous saga in the following sequence:

1. Validate product availability and stock
2. Persist order with status `PENDING`
3. Call Payment Service to process charge
4. Set order status to `CONFIRMED` or `PAYMENT_FAILED`
5. Decrement stock (best-effort, non-fatal on failure)
6. Send notification (fire-and-forget, non-fatal on failure)

### 5. Local DTOs — No Shared Library

Each service declares its own local DTOs for inter-service calls. This avoids shared-library coupling and maintains independent deployability.

---

## API Reference

### Authentication — Public (no JWT required)

```
POST  /api/v1/auth/register     Register a new user and receive a JWT
POST  /api/v1/auth/login        Authenticate and receive a JWT
```

### Users — JWT Required

```
GET    /api/v1/users/me         Retrieve the authenticated user's profile
PATCH  /api/v1/users/me         Update the authenticated user's profile
GET    /api/v1/users            [ADMIN] List all users
GET    /api/v1/users/{id}       [ADMIN] Get user by ID
DELETE /api/v1/users/{id}       [ADMIN] Delete user by ID
```

### Products

```
GET    /api/v1/products                     Paginated list of active products
GET    /api/v1/products/{id}               Get product by ID
GET    /api/v1/products/sku/{sku}          Get product by SKU
GET    /api/v1/products/category/{catId}   Filter products by category
GET    /api/v1/products/search?keyword=    Full-text keyword search
POST   /api/v1/products                    Create a product
PATCH  /api/v1/products/{id}              Partial update of a product
PATCH  /api/v1/products/{id}/inventory    Update stock (ABSOLUTE or DELTA mode)
DELETE /api/v1/products/{id}              Soft-delete a product
```

### Categories

```
GET    /api/v1/categories         List all active categories
GET    /api/v1/categories/{id}    Get category by ID
POST   /api/v1/categories         Create a category
PUT    /api/v1/categories/{id}    Update a category
DELETE /api/v1/categories/{id}    Soft-delete a category
```

### Cart — JWT Required (resolved via Gateway header)

```
GET    /api/v1/cart                  Retrieve the authenticated user's cart
POST   /api/v1/cart/items            Add an item (merges if duplicate)
PATCH  /api/v1/cart/items/{itemId}   Update item quantity (0 removes it)
DELETE /api/v1/cart/items/{itemId}   Remove a specific item
DELETE /api/v1/cart                  Clear the entire cart
```

### Orders — JWT Required (resolved via Gateway header)

```
POST   /api/v1/orders                  Initiate checkout (triggers full saga)
GET    /api/v1/orders/{id}             Get order by ID
GET    /api/v1/orders/user/{userId}    Paginated order history for a user
POST   /api/v1/orders/{id}/cancel      Cancel an order (PENDING or CONFIRMED only)
PATCH  /api/v1/orders/{id}/status      [ADMIN] Update order status
```

### Payments

```
POST   /api/v1/payments/charge         Process a payment
POST   /api/v1/payments/refund         Refund a successful payment
GET    /api/v1/payments/{id}           Get payment by ID
GET    /api/v1/payments/order/{id}     Get payments for an order
GET    /api/v1/payments/user/{id}      Get payment history for a user
```

### Notifications

```
POST   /api/v1/notifications/send               Send a notification
GET    /api/v1/notifications/reference/{id}     Get notifications by reference ID
GET    /api/v1/notifications/recipient?email=   Get notifications by recipient email
```

---

## Security Architecture

```
JWT issued by user-service (HS256, 24-hour expiry)
    |
    +─► Validated by API Gateway on every inbound request
         |
         +─► X-User-Email header injected into downstream service requests
              |
              +─► Downstream services trust this header (no re-validation)
```

| Mechanism          | Implementation                                        |
|--------------------|-------------------------------------------------------|
| Password hashing   | BCrypt (strength 10)                                  |
| JWT secret         | Shared via environment variable                       |
| Session management | Stateless (no HttpSession)                            |
| Role authorisation | `@PreAuthorize("hasRole('ADMIN')")` on admin endpoints |
| Input validation   | Jakarta Bean Validation on all request DTOs           |
| Type coercion      | Rejected — JSON field types are strictly enforced     |

---

## Database Schema

| Service              | Key Tables                                                                        |
|----------------------|-----------------------------------------------------------------------------------|
| user-service         | `users` (id, email, password, role, enabled)                                      |
| product-service      | `products` (id, sku, price, stock_quantity, category_id), `categories`            |
| cart-service         | `carts` (id, user_id, total_price), `cart_items` (product snapshot)               |
| order-service        | `orders` (id, user_id, status, total_amount, payment_txn_id), `order_items`       |
| payment-service      | `payments` (id, transaction_id, order_id, status, method)                         |
| notification-service | `notifications` (id, recipient, subject, type, status, reference_id)              |

---

## Tech Stack

| Concern          | Technology                              |
|------------------|-----------------------------------------|
| Language         | Java 17                                 |
| Framework        | Spring Boot 3.2.x                       |
| Gateway          | Spring Cloud Gateway 2023.x             |
| Security         | Spring Security 6 + JWT (jjwt 0.11.5)  |
| Persistence      | Spring Data JPA + Hibernate             |
| Database         | PostgreSQL 16                           |
| HTTP Client      | Spring WebFlux WebClient                |
| Validation       | Jakarta Bean Validation                 |
| Boilerplate      | Lombok                                  |
| Containerisation | Docker + Docker Compose                 |
| Build            | Maven (multi-module)                    |

---

## Running Locally

### Prerequisites

- Docker Desktop with Compose V2
- Java 17 and Maven (to build JARs prior to Docker image build)

### Step 1: Build all service JARs

```bash
# Run from the project root — builds all modules via the parent POM
mvn clean package -DskipTests
```

### Step 2: Start all containers

```bash
docker compose up --build
```

### Step 3: Verify service health

```bash
# Check container status
docker compose ps

# Verify API Gateway health
curl http://localhost:8080/actuator/health
```

### Step 4: Register and authenticate

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane@example.com",
    "password": "Password1!",
    "phone": "+919876543210"
  }'

# Login to obtain a JWT
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "jane@example.com", "password": "Password1!"}'

# Access a protected endpoint
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### Step 5: End-to-end checkout flow

```bash
TOKEN="<YOUR_JWT_TOKEN>"

# Create a category
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Electronics", "description": "Electronics and Gadgets"}'

# Create a product (use the categoryId returned above)
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "sku": "LAP-001", "price": 59999.0, "stockQuantity": 50, "categoryId": 1}'

# Place an order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "shippingAddress": "123 Main Street, Hyderabad, Telangana",
    "paymentMethod": "CREDIT_CARD",
    "items": [{"productId": 1, "quantity": 2}]
  }'
```

### Tear Down

```bash
# Stop and remove containers; -v removes named volumes and clears database data
docker compose down -v
```

---

## Production Considerations

The following enhancements are recommended before promoting this system to a production environment:

| Area                  | Recommendation                                                      |
|-----------------------|---------------------------------------------------------------------|
| Messaging             | Replace synchronous saga with Kafka or RabbitMQ for resilience      |
| Resilience            | Add retry and circuit-breaker policies via Resilience4j             |
| Service Discovery     | Integrate Spring Cloud Eureka or use Kubernetes DNS                 |
| Configuration         | Centralise configuration with Spring Cloud Config Server            |
| Observability         | Add distributed tracing with Micrometer and Zipkin                  |
| Payments              | Integrate a real payment processor (Stripe, Razorpay)               |
| Notifications         | Integrate JavaMailSender for email and Twilio for SMS               |
| API Versioning        | Enforce versioning via URL path (`/api/v2/`) or request headers     |
