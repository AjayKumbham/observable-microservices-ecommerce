# API Documentation
## Observable Microservices E-Commerce

This document provides a comprehensive technical reference for the RESTful APIs exposed by the Observable Microservices E-Commerce platform. All external communication is routed through a central API Gateway.

---

## 1. Global Configurations

### 1.1 Base URL
All requests must be prefixed with the API Gateway host and the versioned path.
**Base Path:** `http://localhost:8080/api/v1`

### 1.2 Authentication
The system uses JSON Web Tokens (JWT) for authentication. Tokens are obtained via the Authentication Service and must be included in the `Authorization` header for all protected endpoints.

**Header Format:** 
```http
Authorization: Bearer <your_jwt_token>
```

### 1.3 Default Headers
The API Gateway enforces the following headers on all responses:
*   `X-Gateway-Source: api-gateway`
*   `X-Content-Type-Options: nosniff`

All endpoints natively support Cross-Origin Resource Sharing (CORS).

---

## 2. Authentication Service
**Path Prefix:** `/auth`

### 2.1 Register User
**Method:** `POST` | **Path:** `/register` | **Security:** Public

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "phone": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIi...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "role": "USER"
  }
}
```

### 2.2 Login
**Method:** `POST` | **Path:** `/login` | **Security:** Public

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIi...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "role": "USER"
  }
}
```

---

## 3. User Service
**Path Prefix:** `/users`

### 3.1 Get Current User Profile
**Method:** `GET` | **Path:** `/me` | **Security:** Authenticated

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "role": "USER"
}
```

### 3.2 Update Current User Profile
**Method:** `PATCH` | **Path:** `/me` | **Security:** Authenticated

**Request Body:**
```json
{
  "firstName": "Johnny",
  "lastName": "Smith",
  "phone": "+1987654321"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "Johnny",
  "lastName": "Smith",
  "email": "john.doe@example.com",
  "phone": "+1987654321",
  "role": "USER"
}
```

---

## 4. Product Service
**Path Prefix:** `/products` and `/categories`

### 4.1 Create Category
**Method:** `POST` | **Path:** `/categories` | **Security:** Authenticated

**Request Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic gadgets and devices"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic gadgets and devices",
  "active": true
}
```

### 4.2 Create Product
**Method:** `POST` | **Path:** `/products` | **Security:** Authenticated

**Request Body:**
```json
{
  "name": "Gaming Laptop",
  "sku": "LAP-001",
  "price": 1499.99,
  "stockQuantity": 50,
  "categoryId": 1
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "sku": "LAP-001",
  "name": "Gaming Laptop",
  "description": null,
  "price": 1499.99,
  "stockQuantity": 50,
  "active": true,
  "category": {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic gadgets and devices",
    "active": true
  },
  "createdAt": "2026-03-01T12:00:00Z",
  "updatedAt": "2026-03-01T12:00:00Z"
}
```

### 4.3 Update Product Inventory
**Method:** `PATCH` | **Path:** `/products/{id}/inventory` | **Security:** Authenticated

**Request Body:**
```json
{
  "quantity": 10,
  "operation": "ADD" 
}
```
*(Valid operations: ADD, SUBTRACT, SET)*

**Response (200 OK):**
```json
{
  "id": 1,
  "sku": "LAP-001",
  "name": "Gaming Laptop",
  "price": 1499.99,
  "stockQuantity": 60,
  "active": true,
  "category": {
    "id": 1,
    "name": "Electronics"
  }
}
```

### 4.4 List Products (Paginated)
**Method:** `GET` | **Path:** `/products` | **Security:** Authenticated

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "sku": "LAP-001",
      "name": "Gaming Laptop",
      "price": 1499.99,
      "stockQuantity": 60,
      "active": true,
      "category": {
        "id": 1,
        "name": "Electronics"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 5. Cart Service
**Path Prefix:** `/cart`

### 5.1 Add Item to Cart
**Method:** `POST` | **Path:** `/cart/items` | **Security:** Authenticated

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "totalPrice": 2999.98,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Gaming Laptop",
      "sku": "LAP-001",
      "price": 1499.99,
      "quantity": 2,
      "subTotal": 2999.98
    }
  ]
}
```

### 5.2 Update Item Quantity
**Method:** `PATCH` | **Path:** `/cart/items/{itemId}` | **Security:** Authenticated

**Request Body:**
```json
{
  "quantity": 3
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "totalPrice": 4499.97,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Gaming Laptop",
      "sku": "LAP-001",
      "price": 1499.99,
      "quantity": 3,
      "subTotal": 4499.97
    }
  ]
}
```

---

## 6. Order Service
**Path Prefix:** `/orders`

### 6.1 Create Order (Checkout)
**Method:** `POST` | **Path:** `/orders` | **Security:** Authenticated

**Request Body:**
```json
{
  "userId": 1,
  "shippingAddress": "123 Main St, Tech City, ST 12345",
  "paymentMethod": "CREDIT_CARD",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "status": "CONFIRMED",
  "totalAmount": 2999.98,
  "shippingAddress": "123 Main St, Tech City, ST 12345",
  "paymentMethod": "CREDIT_CARD",
  "paymentTxnId": "txn_8f7d6a5b",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Gaming Laptop",
      "sku": "LAP-001",
      "price": 1499.99,
      "quantity": 2
    }
  ],
  "createdAt": "2026-03-01T12:05:00Z",
  "updatedAt": "2026-03-01T12:05:05Z"
}
```

### 6.2 Get Order by ID
**Method:** `GET` | **Path:** `/orders/{id}` | **Security:** Authenticated

**Response (200 OK):**
*(Matches the Create Order response body format)*

---

## 7. Payment Service
**Path Prefix:** `/payments`

### 7.1 Process Charge
**Method:** `POST` | **Path:** `/payments/charge` | **Security:** Authenticated (System Internal Call Typically)

**Request Body:**
```json
{
  "orderId": 1,
  "amount": 2999.98,
  "currency": "USD",
  "method": "CREDIT_CARD",
  "userId": 1
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "transactionId": "txn_8f7d6a5b",
  "orderId": 1,
  "amount": 2999.98,
  "currency": "USD",
  "status": "SUCCESS",
  "method": "CREDIT_CARD",
  "message": "Payment processed successfully"
}
```

---

## 8. Notification Service
**Path Prefix:** `/notifications`

### 8.1 Send Notification
**Method:** `POST` | **Path:** `/notifications/send` | **Security:** Authenticated (System Internal Call Typically)

**Request Body:**
```json
{
  "recipient": "john.doe@example.com",
  "subject": "Order Confirmation - Order #1",
  "content": "Your order has been successfully placed. Total: $2999.98",
  "type": "EMAIL",
  "referenceId": 1
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "recipient": "john.doe@example.com",
  "subject": "Order Confirmation - Order #1",
  "content": "Your order has been successfully placed. Total: $2999.98",
  "type": "EMAIL",
  "status": "SENT",
  "referenceId": 1,
  "sentAt": "2026-03-01T12:05:06Z"
}
```
