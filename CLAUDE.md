# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A simplified, Uber-Eats-like food delivery app used to demonstrate a Kafka-driven,
event-notification microservice architecture: customer places an order → restaurant
marks it ready → courier picks it up and delivers it. Deliberately simple — no auth,
no real payments, no event sourcing.

## Commands

Backend (Gradle multi-module monorepo — root `build.gradle` declares shared
dependencies/config for all three service subprojects):

```
gradle build                                  # build + test all 3 services
gradle :order-service:test                    # test a single service
gradle :order-service:test --tests "*OrderControllerTest"   # a single test class
gradle :order-service:bootRun                 # run one service locally (needs Postgres + Kafka reachable)
```

No `gradlew` wrapper is checked in — use a system-installed `gradle`, or generate
one with `gradle wrapper --gradle-version 8.10`.

Frontend (`frontend/`, separate npm project, not part of the Gradle build):

```
cd frontend && npm install
npm run dev            # Vite dev server on :5173
npm run build          # production build to frontend/dist
```

Local end-to-end run: see README.md for the full `kind` + `kubectl apply -f k8s/`
sequence, including per-service `docker build` commands (each Dockerfile builds from
the repo root since it needs the whole Gradle monorepo to build its module).

## Architecture

Three independent Spring Boot (Java 17) services, each owning its own Postgres
**schema** (`order_service`, `restaurant_service`, `delivery_service`) in one shared
Postgres instance. A service never reads another service's tables — Kafka events are
the only contract between them:

```
order-service --Kafka: order.placed--> restaurant-service --Kafka: order.ready--> delivery-service
     ^                                                                                  |
     +---------------------------- Kafka: delivery.status.updated ---------------------+
```

- **order-service** (`order-service/`, port 8081): owns `Order`/`OrderItem`.
  `POST /orders` publishes `order.placed`. Consumes `order.ready` and
  `delivery.status.updated` (two different event types, two different topics) to
  advance `Order.status` through `PLACED → READY → PICKED_UP → DELIVERED`.
- **restaurant-service** (`restaurant-service/`, port 8082): owns `Restaurant`,
  `MenuItem`, and `QueueEntry` (its local, denormalized view of an order, populated
  from `order.placed`). `POST /queue/{orderId}/ready` publishes `order.ready`.
- **delivery-service** (`delivery-service/`, port 8083): owns `Courier`, `Delivery`.
  Consuming `order.ready` auto-assigns the next available courier. `POST
  /deliveries/{id}/picked-up` and `/delivered` publish `delivery.status.updated`.

Each service defines its **own copy** of the Kafka event classes it needs (e.g.
`order-service`'s `kafka/events/OrderReadyEvent.java` and `restaurant-service`'s
`kafka/events/OrderReadyEvent.java` are separate classes with matching fields) — there
is no shared library module. The JSON payload shape is the actual contract, not a
shared Java type.

**Kafka JSON deserialization gotcha**: Spring Boot's default JSON consumer
configuration assumes one message type per service. `order-service` consumes *two*
event types (`OrderReadyEvent` and `DeliveryStatusUpdatedEvent`) from two topics, so it
can't use that default — see `order-service/.../kafka/KafkaConsumerConfig.java`, which
defines one typed `ConcurrentKafkaListenerContainerFactory` per event type (each
`ignoreTypeHeaders()`, since the producer's type header would reference a class in the
*producer's* package, not the consumer's). `restaurant-service` and `delivery-service`
each consume only one event type, so they use the simpler
`spring.kafka.consumer.properties.spring.json.value.default.type` (plus
`spring.json.use.type.headers: false`) properties in their `application.yml` instead.

Each service's `application.yml` reads DB/Kafka connection info from env vars with
localhost-friendly defaults (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`,
`DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`) — these are populated via the `app-config`
ConfigMap and `postgres-secret` Secret in `k8s/config-secret.yaml`. Restaurant and
courier/menu seed data lives in each service's `src/main/resources/data.sql`;
`order-service` has none (orders are only ever created by customers, never seeded).
Postgres schemas themselves are created once via the init script in
`k8s/postgres.yaml`, not by any service — Hibernate's `ddl-auto: update` runs before
`data.sql` and needs the schema to already exist.

The frontend (`frontend/`, React + Vite, no state library) calls all three backend
services directly from the browser via separate base URLs in `src/api/client.js`
(defaulting to `localhost:8081/8082/8083`, overridable via `VITE_*_SERVICE_URL`) —
there's no gateway/BFF. Each backend service has an open, permissive CORS
`WebConfig` for this reason. Routing between the three pages (Customer, Restaurant,
Courier) is client-side via `react-router-dom`'s `HashRouter`.
