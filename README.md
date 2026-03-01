# 🛒 Observable Microservices E-Commerce Backend

A **production-quality, portfolio-grade** microservices e-commerce backend built with **Spring Boot 3**, **Java 17**, and **PostgreSQL**. Implements a full checkout saga across 7 independently deployable services, all accessible through a central **Spring Cloud Gateway**.

---

## 📐 System Architecture

```
                          ┌──────────────────────┐
         Client Requests  │     API Gateway       │  :8080
         ───────────────► │  Spring Cloud Gateway  │
                          │  JWT Auth Filter       │
                          └──────────┬───────────┘
                                     │ Routes by path
              ┌──────────────────────┼──────────────────────────┐
              │                      │                           │
              ▼                      ▼                           ▼
     ┌─────────────────┐   ┌──────────────────┐   ┌─────────────────────┐
     │  User Service   │   │ Product Service  │   │    Cart Service     │
     │  :8081          │   │  :8082           │   │    :8083            │
     │  • Register     │   │  • Catalog       │   │    • Add/Remove     │
     │  • Login (JWT)  │   │  • Categories    │   │    • Qty Update     │
     │  • Profiles     │   │  • Inventory     │   │    • Auto-total     │
     └─────────────────┘   └──────────────────┘   └─────────────────────┘
           │                        ▲                      │
           │ own DB                 │ WebClient            │ WebClient
           ▼                        │                      ▼
      [user_db]             ┌───────────────────────────────────────┐
                            │          Order Service  :8084          │
                            │  • Checkout saga orchestrator          │
                            │  • Calls: Product → Payment → Notify   │
                            └───────────┬───────────────────────────┘
                                        │
              ┌─────────────────────────┴────────────────────────┐
              │                                                   │
              ▼                                                   ▼
  ┌──────────────────────┐                     ┌───────────────────────────┐
  │   Payment Service    │                     │   Notification Service    │
  │   :8085              │                     │   :8086                   │
  │   • Mock charge      │                     │   • Mock Email/SMS        │
  │   • Refund           │                     │   • Audit log of sends    │
  │   • 90% success rate │                     │                           │
  └──────────────────────┘                     └───────────────────────────┘
```

---

## 🏗️ Microservices Overview

| Service | Port | Database | Responsibility |
|---|---|---|---|
| **API Gateway** | 8080 | — | Route all requests, JWT validation, CORS |
| **User Service** | 8081 | user_db | Register, Login (JWT), Profile CRUD |
| **Product Service** | 8082 | product_db | Catalog, Categories, Inventory management |
| **Cart Service** | 8083 | cart_db | Per-user cart, denormalised product snapshots |
| **Order Service** | 8084 | order_db | Checkout saga (product → payment → notify) |
| **Payment Service** | 8085 | payment_db | Mock payment processing, refunds |
| **Notification Service** | 8086 | notification_db | Mock Email/SMS dispatch + audit |

---

## 🔑 Key Design Decisions

### 1. JWT at the Gateway
JWT tokens are validated **once** at the API Gateway. The gateway injects `X-User-Email` and `X-Auth-Token` headers into downstream requests. Downstream services **never handle raw JWTs** — they rely on trusted headers set by the gateway.

### 2. Database-per-Service
Each service owns its schema. No JOINs across service boundaries. This enforces bounded contexts and allows independent schema evolution and scaling.

### 3. Denormalised Data in Cart & Order Items
Cart items and order items **snapshot** product data (name, SKU, price) at the time of add/checkout. This ensures historical accuracy — changing product price doesn't alter past orders/carts.

### 4. Checkout Saga (Synchronous)
The `OrderService` orchestrates a synchronous saga:
1. Validate product availability + stock
2. Persist order as `PENDING`
3. Call payment-service
4. Set `CONFIRMED` or `PAYMENT_FAILED`
5. Decrement stock (best-effort, non-fatal)
6. Send notification (fire-and-forget, non-fatal)

### 5. Local DTO Copies (No Shared JAR)
Each service declares its own local DTOs for inter-service calls. This avoids shared-library coupling and maintains independent deployability.

---

## 📋 API Endpoints

### Auth (Public — no JWT needed)
```
POST /api/v1/auth/register     # Register new user → returns JWT
POST /api/v1/auth/login        # Login → returns JWT
```

### Users (JWT required)
```
GET    /api/v1/users/me        # Get own profile
PATCH  /api/v1/users/me        # Update own profile
GET    /api/v1/users           # [ADMIN] List all users
GET    /api/v1/users/{id}      # [ADMIN] Get user by ID
DELETE /api/v1/users/{id}      # [ADMIN] Delete user
```

### Products
```
GET    /api/v1/products                     # Paginated list (active only)
GET    /api/v1/products/{id}               # Product by ID
GET    /api/v1/products/sku/{sku}          # Product by SKU
GET    /api/v1/products/category/{catId}   # Filter by category
GET    /api/v1/products/search?keyword=    # Keyword search
POST   /api/v1/products                    # Create product
PATCH  /api/v1/products/{id}              # Partial update
PATCH  /api/v1/products/{id}/inventory    # Update stock (ABSOLUTE | DELTA)
DELETE /api/v1/products/{id}              # Soft-delete
```

### Categories
```
GET    /api/v1/categories       # All active categories
GET    /api/v1/categories/{id}  # Category by ID
POST   /api/v1/categories       # Create
PUT    /api/v1/categories/{id}  # Update
DELETE /api/v1/categories/{id}  # Soft-delete
```

### Cart (JWT required via Gateway)
```
GET    /api/v1/cart                   # Get user's cart (header: X-User-Id)
POST   /api/v1/cart/items             # Add item (merges if duplicate)
PATCH  /api/v1/cart/items/{itemId}   # Update qty (0 = remove)
DELETE /api/v1/cart/items/{itemId}   # Remove item
DELETE /api/v1/cart                   # Clear entire cart
```

### Orders (JWT required via Gateway)
```
POST   /api/v1/orders                  # Checkout (triggers full saga)
GET    /api/v1/orders/{id}             # Order by ID
GET    /api/v1/orders/user/{userId}    # Paginated orders for user
POST   /api/v1/orders/{id}/cancel      # Cancel (PENDING/CONFIRMED only)
PATCH  /api/v1/orders/{id}/status      # Update status (admin)
```

### Payments
```
POST   /api/v1/payments/charge         # Process payment
POST   /api/v1/payments/refund         # Refund (SUCCESS → REFUNDED)
GET    /api/v1/payments/{id}           # Payment by ID
GET    /api/v1/payments/order/{id}     # Payments for order
GET    /api/v1/payments/user/{id}      # Payments for user
```

### Notifications
```
POST   /api/v1/notifications/send              # Send notification
GET    /api/v1/notifications/reference/{id}   # By referenceId (orderId etc.)
GET    /api/v1/notifications/recipient?email= # By recipient
```

---

## 📂 Folder Structure (per service)

```
user-service/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/com/ecommerce/userservice/
    │   ├── UserServiceApplication.java
    │   ├── config/
    │   │   ├── JpaConfig.java              # Enable JPA Auditing
    │   │   ├── JwtAuthenticationFilter.java
    │   │   └── SecurityConfig.java
    │   ├── controller/
    │   │   ├── AuthController.java         # POST /auth/register, /auth/login
    │   │   └── UserController.java         # GET/PATCH /users/me, admin endpoints
    │   ├── dto/
    │   │   ├── RegisterRequest.java
    │   │   ├── LoginRequest.java
    │   │   ├── AuthResponse.java
    │   │   ├── UserProfileDto.java
    │   │   └── UpdateProfileRequest.java
    │   ├── exception/
    │   │   ├── ApiException.java
    │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
    │   │   ├── UserNotFoundException.java
    │   │   └── UserAlreadyExistsException.java
    │   ├── model/
    │   │   ├── User.java
    │   │   └── UserRole.java
    │   ├── repository/
    │   │   └── UserRepository.java
    │   └── service/
    │       ├── JwtService.java
    │       ├── UserDetailsServiceImpl.java
    │       └── UserService.java
    └── resources/
        └── application.yml
```

---

## 🔗 Inter-Service Communication

```
Cart Service      ──WebClient──► Product Service     (fetch product for snapshot)
Order Service     ──WebClient──► Product Service     (validate stock + decrement)
Order Service     ──WebClient──► Payment Service     (process charge)
Order Service     ──WebClient──► Notification Service (send confirmation email)
```

All inter-service calls use **Spring WebFlux WebClient** (synchronous `.block()` for simplicity).

---

## 🐳 Running Locally with Docker Compose

### Prerequisites
- Docker Desktop (with Compose V2)
- Java 17 + Maven (to build JARs)

### Step 1: Build all service JARs
```bash
# From project root — builds all modules via parent POM
mvn clean package -DskipTests
```

### Step 2: Start everything
```bash
docker compose up --build
```

### Step 3: Verify all services are healthy
```bash
# Check all containers
docker compose ps

# Test API Gateway
curl http://localhost:8080/actuator/health
```

### Step 4: Make your first API call
```bash
# Register a user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ajay",
    "lastName": "Goud",
    "email": "ajay@example.com",
    "password": "Password1",
    "phone": "+919876543210"
  }'

# Login → get token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "ajay@example.com", "password": "Password1"}'

# Use token for authenticated calls
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### Step 5: Checkout flow
```bash
TOKEN="<YOUR_JWT_TOKEN>"

# 1. Create a category
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Electronics", "description": "Electronics & Gadgets"}'

# 2. Create a product (categoryId from above)
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "sku": "LAP-001", "price": 59999.0, "stockQuantity": 50, "categoryId": 1}'

# 3. Place an order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "shippingAddress": "123 Main St, Hyderabad, Telangana",
    "paymentMethod": "CREDIT_CARD",
    "items": [{"productId": 1, "quantity": 2}]
  }'
```

### Tear down
```bash
docker compose down -v    # -v removes named volumes (clears DB data)
```

---

## 🗃️ Database Schema Summary

| Service | Key Tables |
|---|---|
| user-service | `users` (id, email, password, role, enabled) |
| product-service | `products` (id, sku, price, stock_quantity, category_id), `categories` |
| cart-service | `carts` (id, user_id, total_price), `cart_items` (snapshot of product) |
| order-service | `orders` (id, user_id, status, total_amount, payment_txn_id), `order_items` |
| payment-service | `payments` (id, transaction_id, order_id, status, method) |
| notification-service | `notifications` (id, recipient, subject, type, status, reference_id) |

---

## 🛡️ Security Architecture

```
JWT issued by user-service (HS256, 24h expiry)
    │
    └─► Validated by API Gateway on every request
         │
         └─► X-User-Email header injected into downstream requests
              │
              └─► Downstream services trust this header (no re-validation)
```

- Passwords encoded with **BCrypt** (strength 10)
- JWT secret shared via environment variable (same in gateway + user-service)
- Stateless sessions (no HttpSession)
- Method-level security via `@PreAuthorize("hasRole('ADMIN')")`

---

## ⚙️ Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.x |
| Gateway | Spring Cloud Gateway 2023.x |
| Security | Spring Security 6 + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 |
| HTTP Client | Spring WebFlux WebClient |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok + MapStruct |
| Containerisation | Docker + Docker Compose |
| Build | Maven (multi-module) |

---

## 🚀 Production Considerations (Next Steps)

- **Replace synchronous saga** with async messaging (Kafka/RabbitMQ) for resilience
- **Retry + Circuit Breaker** via Resilience4j on WebClient calls
- **Service Discovery** via Spring Cloud Eureka or Kubernetes DNS
- **Centralised Config** via Spring Cloud Config Server
- **Distributed Tracing** via Micrometer + Zipkin
- **Real Payments** via Stripe/Razorpay SDK in PaymentService
- **Real Notifications** via JavaMailSender (email) + Twilio (SMS) in NotificationService
- **API Versioning** via path (`/api/v2/`) or header-based routing
#   o b s e r v a b l e - m i c r o s e r v i c e s - e c o m m e r c e  
 