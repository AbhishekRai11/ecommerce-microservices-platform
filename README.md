# E-Commerce Microservices Platform

A portfolio-grade distributed e-commerce backend built with Java 17, Spring Boot, Spring Cloud, Kafka, MySQL, Redis, JWT and Docker.

## Architecture

Client → API Gateway → Eureka-discovered services

| Service | Responsibility | Port |
|---|---|---:|
| Service Discovery | Eureka registry | 8761 |
| API Gateway | Single entry point + JWT validation | 8080 |
| User Service | Registration, login, profiles, roles | 8081 |
| Product Service | Catalog, categories, CRUD, stock reservation, Redis cache | 8082 |
| Order Service | Orders, history, cancellation, stock reservation | 8083 |
| Payment Service | Mock asynchronous payment processing | 8084 |
| Notification Service | Kafka event consumer | 8085 |

Each core domain owns its own MySQL database. Kafka is used for asynchronous order/payment events and Redis caches product reads.

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Cloud Gateway, Eureka and OpenFeign
- Spring Data JPA / Hibernate
- Spring Security Crypto + JWT
- Apache Kafka
- MySQL 8.4
- Redis 7
- Bean Validation
- Maven
- Docker Compose
- GitHub Actions

## Request Flow

1. Register: POST `/api/users/register`
2. Login: POST `/api/users/login`
3. Send the JWT as `Authorization: Bearer <token>`.
4. Gateway validates the token and forwards `X-User-Id` and `X-User-Role`.
5. Order Service calls Product Service through OpenFeign.
6. Product stock is reserved.
7. Order Service publishes `ORDER_CREATED` to Kafka.
8. Payment Service consumes it, creates a mock successful payment and publishes `PAYMENT_SUCCESS`.
9. Order Service consumes the payment event and changes the order to `PAID`.
10. Notification Service consumes order/payment events and logs a notification message.

This is an eventual-consistency workflow rather than a distributed database transaction.

## API Endpoints

### Users
- POST `/api/users/register`
- POST `/api/users/login`
- GET `/api/users/me`

### Products
- GET `/api/products`
- GET `/api/products/{id}`
- POST `/api/products`
- PUT `/api/products/{id}`
- DELETE `/api/products/{id}`
- POST `/api/products/{id}/reserve?quantity=1`
- POST `/api/products/{id}/release?quantity=1`

### Orders
- POST `/api/orders`
- GET `/api/orders`
- GET `/api/orders/{id}`
- POST `/api/orders/{id}/cancel`

## Sample Requests

Register:
```json
{"name":"Abhishek","email":"user@example.com","password":"Password123"}
```

Login:
```json
{"email":"user@example.com","password":"Password123"}
```

Create product:
```json
{"sku":"KB-001","name":"Mechanical Keyboard","category":"electronics","price":4999,"stock":20}
```

Create order:
```json
{"items":[{"productId":"PRODUCT_UUID","quantity":2}]}
```

## Run Locally

Prerequisites: JDK 17, Maven 3.9+, Docker.

```bash
docker compose up -d mysql-user mysql-product mysql-order mysql-payment redis kafka
mvn clean package -DskipTests
```

Then either run each Spring Boot service from the IDE or start the full container stack:

```bash
docker compose up --build
```

For local IDE execution, use the default ports above. For Docker execution, the compose file supplies service-to-service hostnames and the JWT secret.

## Database Architecture

- `user_db`
- `product_db`
- `order_db`
- `payment_db`

The services do not share JPA entities or a database. Cross-service communication uses REST/OpenFeign or Kafka events.

## Kafka Topics

- `order-events`
- `payment-events`

Shared event contract:
`eventType, eventId, aggregateId, userId, payload, occurredAt`

## Redis

Product catalog reads use Spring Cache backed by Redis. Mutating product/stock operations evict the relevant cache entries so the catalog does not keep stale stock indefinitely.

## Security

Passwords are stored with BCrypt. JWTs contain the user ID, role and email. The gateway rejects missing/invalid bearer tokens for protected routes. No production secrets are committed; replace the development JWT secret before deployment.

## Project Structure

```
ecommerce-microservices-platform/
├── common-events/
├── service-discovery/
├── api-gateway/
├── user-service/
├── product-service/
├── order-service/
├── payment-service/
├── notification-service/
├── docker-compose.yml
├── .env.example
└── .github/workflows/ci.yml
```

## Resume Highlights

- Designed a distributed e-commerce backend with independently deployable Spring Cloud services.
- Implemented JWT authentication at an API Gateway and Eureka service discovery.
- Implemented Kafka-based asynchronous payment and notification workflows.
- Used database-per-service MySQL isolation and OpenFeign for synchronous service calls.
- Added Redis caching and stock reservation to the product domain.
- Containerized infrastructure and services with Docker Compose and added Maven CI.

## Future Enhancements

Razorpay/Stripe adapter, Resilience4j circuit breakers, refresh tokens, transactional outbox, OpenTelemetry tracing, centralized configuration, Testcontainers integration tests and Kubernetes deployment.

> Portfolio note: the payment service is intentionally a mock processor. It demonstrates the distributed workflow without handling real money or credentials.
