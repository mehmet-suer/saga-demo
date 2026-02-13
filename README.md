# Saga Pattern Microservices Implementation

A distributed e-commerce system demonstrating **Saga Pattern** with **Choreography** approach using **Event-Driven Architecture**.

## Features

- **Saga Pattern** with Choreography
- **Real Compensation Logic** (Payment cancellation on inventory failure)
- **Event-Driven Architecture** with Apache Kafka
- **Outbox Pattern** for reliable event publishing
- **Advanced Idempotency** (EventType-based)
- **Dead Letter Queue** for error handling
- **Event Validation** with Bean Validation

## Tech Stack

- **Java 21** + **Spring Boot 3.5.4**
- **Apache Kafka** for event streaming
- **H2 Database** with **Flyway** migrations
- **JPA/Hibernate** for data persistence
- **Gradle** build system

## Quick Start

### Prerequisites
- Java 21
- Apache Kafka (localhost:9092)

### Run Services
```bash
# Order Service (Port 9005)
cd order-service && ./gradlew bootRun

# Payment Service (Port 9006)  
cd payment-service && ./gradlew bootRun

# Inventory Service (Port 9007)
cd inventory-service && ./gradlew bootRun
```

### Test Saga Flow
```bash
# Create Order
curl --location --request POST 'http://localhost:9005/api/orders?userId=24234&productId=15'

# Init Product
curl --location --request POST 'http://localhost:9007/api/inventory/init?productId=15&qty=5000'
```

## Saga Flow

1. **Order Created** → `OrderCreatedEvent` published
2. **Payment Processed** → `OrderPaymentCompletedEvent` or `OrderPaymentFailedEvent`
3. **Inventory Reserved** → `InventoryReservationCompletedEvent` or `InventoryReservationFailedEvent`
4. **Compensation** → Payment automatically cancelled if inventory fails

## Key Components

- **Event Correlation**: `eventId` + `traceId` for saga tracking
- **Idempotency**: Prevents duplicate event processing
- **Outbox Pattern**: Ensures event delivery reliability
- **Compensation**: Automatic rollback on failures

## Project Structure

```
├── order-service/          # Order management & saga orchestration
├── payment-service/        # Payment processing & compensation
├── inventory-service/      # Inventory reservation
└── order-status-streamer/  # Real-time order status aggregation
```

