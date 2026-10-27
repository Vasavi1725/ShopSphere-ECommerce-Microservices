# ShopSphere — Distributed E-Commerce Microservices

## Overview
ShopSphere is a Java-based distributed e-commerce project built using independently running microservices. It demonstrates REST API communication, persistent data storage, and order-related workflows.

## Technology Stack
- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL (Neon)
- Gradle
- REST APIs
- Spring Security and HTTP Basic authentication
- Git and GitHub

## Microservices
| Service | Port | Responsibility |
|---|---:|---|
| Order Service | 8081 | Order APIs and order workflow |
| Product Service | 8082 | Product and inventory operations |
| Payment Service | 8083 | Payment record management |
| Notification Service | 8086 | Notification record creation and retrieval |

## Architecture
The services expose REST endpoints and use PostgreSQL persistence. The Order Service is configured to communicate with the Notification Service during order confirmation. Payment and product operations are handled by their respective services.

## Prerequisites
- JDK 17
- Git
- PostgreSQL database access
- Gradle Wrapper included in the repository

## Configuration
Configure database connection settings using environment variables or the application's local configuration. Never commit database passwords, API secrets, or `.env` files.

Set the required database URL, username, and password for each service before starting it.

## Running the Services
Run each service from the repository root in a separate terminal.

**Order Service**
```powershell
.\gradlew.bat :bootRun
```

**Payment Service**
```powershell
.\gradlew.bat -p payment-service bootRun
```

**Notification Service**
```powershell
.\gradlew.bat -p notification-service bootRun --args="--server.port=8086"
```

Start Product Service using its own Gradle project and configured port.

## API Overview
- Order Service: `/orders`
- Authentication: `/auth/signup`
- Payment Service: `/payments`
- Notification Service: `/notifications`
- Product Service: `/products`

Use the controllers in each service for the supported HTTP methods, request bodies, and response formats.

## Testing
Run the relevant Gradle test tasks for each service. Verify authentication, database persistence, payment processing, and notification creation before demonstrating the complete order workflow.

## Current Status
This repository contains the implemented services and ongoing integration work. End-to-end behavior should be verified in the target environment before claiming the full workflow is production-ready.

## Security Notes
- Keep credentials outside source control.
- Use HTTPS and production-grade authentication for deployment.
- Replace test recipient addresses and local service URLs before production deployment.

## Future Improvements
- Kafka-based event-driven communication
- Saga orchestration and reliable distributed transactions
- Docker Compose and Kubernetes deployment
- OAuth2/JWT authentication
- Automated integration and end-to-end tests
