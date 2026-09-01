# Food Delivery App (Kafka demo)

A simplified, Uber-Eats-like food delivery app used to demonstrate a Kafka-driven,
event-notification microservice architecture. No auth, no real payments, no event
sourcing — just the core flow: a customer places an order, the restaurant marks it
ready, a courier picks it up and delivers it.

## Architecture

Three independent Spring Boot services, each owning its own Postgres schema and
communicating only through Kafka events (never by reading another service's tables):

- **order-service** (`:8081`) — owns orders. Publishes `order.placed`. Consumes
  `order.ready` and `delivery.status.updated` to keep an order's status current.
- **restaurant-service** (`:8082`) — owns restaurants, menus, and a kitchen queue.
  Consumes `order.placed`. Publishes `order.ready` when a restaurant marks an order
  ready.
- **delivery-service** (`:8083`) — owns couriers and deliveries. Consumes
  `order.ready` (auto-assigns the next available courier). Publishes
  `delivery.status.updated` as a courier picks up / delivers an order.

All three share one Postgres instance but each uses its own schema
(`order_service`, `restaurant_service`, `delivery_service`) — see `k8s/postgres.yaml`
for the schema-creation init script. Kafka runs as a single KRaft-mode broker (no
Zookeeper) — see `k8s/kafka.yaml`.

A React (Vite) frontend (`frontend/`) provides three simple views: Customer,
Restaurant, Courier — each a dropdown-driven page (no login) for that role.

## Building

This is a Gradle multi-module monorepo (`order-service`, `restaurant-service`,
`delivery-service`) plus a separate `frontend/` npm project.

```
gradle build          # compiles and tests all 3 services
cd frontend && npm install && npm run build
```

> **Note:** This repo does not include a generated `gradlew` wrapper. Generate one
> locally with `gradle wrapper --gradle-version 8.10` if you'd like `./gradlew`
> instead of a system-installed `gradle`.

## Running locally on Kubernetes

Requires Docker, `kind`, and `kubectl`.

1. Create a local cluster: `kind create cluster`
2. Build and load each image (run from the repo root, since each Dockerfile builds
   the whole monorepo to resolve its Gradle module):
   ```
   docker build -f order-service/Dockerfile -t order-service:local .
   docker build -f restaurant-service/Dockerfile -t restaurant-service:local .
   docker build -f delivery-service/Dockerfile -t delivery-service:local .
   docker build -f frontend/Dockerfile -t frontend:local frontend/

   kind load docker-image order-service:local restaurant-service:local delivery-service:local frontend:local
   ```
3. Apply the manifests: `kubectl apply -f k8s/`
4. Port-forward everything the frontend needs (it's built to call these ports by
   default):
   ```
   kubectl port-forward svc/order-service 8081:8081 &
   kubectl port-forward svc/restaurant-service 8082:8082 &
   kubectl port-forward svc/delivery-service 8083:8083 &
   kubectl port-forward svc/frontend 8080:80 &
   ```
5. Open http://localhost:8080

## Walking the golden path

1. **Customer** tab: pick a restaurant, add a couple of menu items, enter a name,
   place the order.
2. **Restaurant** tab: pick the same restaurant, see the new order in the queue,
   click "Mark ready".
3. **Courier** tab: pick the courier that got auto-assigned (check `available`/
   `busy` in the dropdown), mark the delivery "picked up" then "delivered".
4. Back on the **Customer** tab, the order's status should progress from `PLACED`
   → `READY` → `PICKED_UP` → `DELIVERED` (polled every 3s).

## Known simplifications

- No auth — roles are just dropdown selections, not real accounts.
- No real payments — placing an order doesn't charge anything.
- If no courier is available when an order becomes ready, delivery-service silently
  drops the assignment (there's no retry/backlog) — the seed data provides 3
  couriers, so this only matters if you place several concurrent orders.
- Postgres and Kafka use `emptyDir` storage in `k8s/postgres.yaml` — data does not
  survive a pod restart. Fine for a demo, not for anything persistent.
- Kafka runs as a single broker with no replication — a real deployment would run
  a multi-broker cluster.

## Scaling considerations

This demo is intentionally sized for a single-node `kind` cluster. A few things
would need to change before it could handle real production load:

- **Kafka topics are single-partition and consumers are single-threaded.** No
  service defines a `NewTopic` bean, so `order.placed`, `order.ready`, and
  `delivery.status.updated` all get created with the broker's default of 1
  partition, and no `@KafkaListener` sets `concurrency` — so each service
  processes a topic on exactly one thread regardless of load. Scaling this out
  means creating topics with multiple partitions keyed by `restaurantId` (for
  `order.placed`/`order.ready`) or `courierId`/`orderId` (for
  `delivery.status.updated`) and raising listener concurrency to match. This
  trades away Kafka's global ordering guarantee for per-key ordering only, which
  is acceptable here since nothing depends on cross-restaurant or cross-courier
  ordering.
- **Read-then-write races need explicit row locking.** delivery-service's
  courier assignment (`OrderReadyConsumer`) uses `SELECT ... FOR UPDATE SKIP
  LOCKED` inside a `@Transactional` method rather than a plain `SELECT`,
  specifically so that once partitions/concurrency are added, two consumer
  threads can't both read the same courier as available before either commits.
  Any other "find an available X and claim it" query added later should follow
  the same pattern rather than relying on Postgres to lock reads for you (it
  doesn't, under the default `READ COMMITTED` isolation level).
- **One shared Postgres instance.** All three services currently share a single
  Postgres instance (separated only by schema). That's fine for a demo, but it's
  a shared scaling and availability bottleneck — a real deployment would give
  each service its own database (or at least its own instance), consistent with
  services never reading each other's tables today.
- **No gateway/BFF.** The frontend calls all three services' base URLs directly.
  That's not a scaling problem by itself, but a real deployment would likely add
  a gateway for TLS termination, rate limiting, and auth — none of which exist
  here.
