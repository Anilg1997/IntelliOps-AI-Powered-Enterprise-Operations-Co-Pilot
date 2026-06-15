# Technology Decisions

## Why PostgreSQL for Order & Customer Service?

**PostgreSQL** was chosen as the primary database for the Order Service because orders, payments, and customer data require **ACID transactions** and **relational integrity**. An order line item must reference a valid order and a valid product — this is a textbook relational constraint. PostgreSQL's mature transaction model, foreign key enforcement, and strong consistency guarantees make it the correct choice for a **system of record**.

**Alternative considered:** MongoDB would give us a flexible schema, but the financial nature of order data (where double-entry bookkeeping and audit trails matter) makes relational integrity a hard requirement. PostgreSQL also gives us pgvector extension for Phase 3's RAG pipeline, letting us use the same database for both transactional and vector workloads.

**Tradeoff:** Fixed relational schema means schema migrations (via Flyway) are necessary when adding new fields. This is a well-understood operational cost.

## Why MongoDB for Inventory & Product Catalog?

The **Inventory & Product Catalog Service** uses MongoDB because **product attributes vary wildly by category**. An electronics SKU has specs like RAM, storage, and processor speed; a clothing SKU has size, color, and material. A rigid relational schema would require either huge nullable column sets or an EAV (entity-attribute-value) anti-pattern. MongoDB's document model lets each product have exactly the attributes it needs — `{ "category": "electronics", "specs": { "ram": "16GB", "storage": "512GB" } }`.

**Alternative considered:** PostgreSQL with JSONB columns could work, but MongoDB's native indexing on nested fields, aggregation pipeline, and horizontal scaling story make it a better fit for a catalog that may grow to millions of SKUs.

**Tradeoff:** No built-in joins between collections. The application layer must handle aggregation. Eventual consistency is acceptable here because inventory data is not a system of record (order data in PostgreSQL is).

## Why REST (for external APIs) + GraphQL (as BFF)?

The Order Service exposes **two API styles** for different consumers:

- **REST** (`/api/v1/orders`, `/api/v1/customers`, `/api/v1/products`) is used for partner/external integrations and admin operations. REST is the industry standard — it's versionable, cacheable, and has ubiquitous tooling. When a billing system or partner portal needs to create an order, they use REST.

- **GraphQL** (via `spring-boot-starter-graphql`) is used as a **backend-for-frontend (BFF)** layer. The Angular dashboard needs to display an order together with customer info, line items, and product details on a single screen. Over REST, this means either over-fetching from multiple endpoints or building custom aggregation endpoints. GraphQL lets the frontend query exactly the shape it needs in one round trip.

**Alternative considered:** Using only GraphQL for everything would simplify the API surface, but not all consumers benefit from GraphQL's flexibility — external APIs benefit from REST's predictable URL structure and HTTP caching.

**Tradeoff:** Maintaining two API styles adds complexity. The REST controllers are thin wrappers around the same service layer the GraphQL resolvers call, so there's no duplicated business logic — just dual API surface management.

## Why gRPC for Inventory Service calls?

When the Order Service confirms an order, it must **check and decrement stock synchronously** — this is on the hot path and happens many times per second. gRPC uses **Protocol Buffers** (binary serialization) and HTTP/2, giving it significantly lower latency and higher throughput than REST/JSON for internal service-to-service calls.

**Alternative considered:** REST/JSON is simpler and more debuggable, but for a high-frequency synchronous call where every millisecond counts, gRPC's performance advantage is meaningful. Spring Boot 3's gRPC support (via grpc-spring-boot-starter) makes this straightforward to implement.

**Tradeoff:** gRPC clients and servers share tightly-coupled protobuf contracts. Versioning proto files requires discipline (backward-compatible field numbering, avoiding breaking changes). REST is retained for external APIs where versioning is simpler.

## Why Oracle + SOAP for Legacy Billing?

The **Legacy Billing Adapter Service** deliberately simulates an **enterprise legacy system** using Oracle Database and SOAP web services. In the real world, telecom, banking, insurance, and retail companies universally have billing/ERP platforms that run on Oracle with SOAP/WSDL contracts that cannot be easily replaced. Building an adapter that:

1. Talks **SOAP** to a legacy system (via Spring-WS)
2. Stores data in **Oracle DB**
3. Exposes a clean MCP interface to the AI co-pilot

...demonstrates **legacy integration skills**. This is one of the highest-value, least-glamorous skills in enterprise development — and most GenAI portfolios completely ignore it.

**Tradeoff:** Oracle XE Docker image is ~3GB and has licensing restrictions for production use. For local development, it runs fine in Docker. In production, this would run on RDS for Oracle or remain on-prem. We explicitly note this in our deployment docs as a pragmatic cost decision.

## Why Kafka for Event-Driven Communication?

Kafka decouples the Order Service from downstream consumers (notification service, activity log, analytics). When an order is created or its status changes, the Order Service publishes an event to a Kafka topic and continues processing — it doesn't need to wait for email sending, log writing, or audit trail updates.

More importantly for the AI story, Kafka creates an **audit trail / timeline** that the AI co-pilot can query later. The activity log (stored in MongoDB by the notification service) is the source of truth for "what happened to this order and when" — the AI agent uses this timeline to synthesize answers.

**Alternative considered:** Direct REST calls from Order Service to Notification Service would be simpler but tightly couples the services and blocks the order processing pipeline. RabbitMQ/PubSub+ would work but Kafka's log-based storage gives us replayability and the audit trail property that's essential for the AI use case.

**Tradeoff:** Kafka adds operational complexity — ZooKeeper, topic management, consumer group coordination. The at-least-once delivery semantics require consumers to be idempotent.

## Why Ollama for AI (vs. OpenAI / Anthropic)?

**Ollama** runs large language models **entirely locally** — no data ever leaves the network. For an enterprise operations co-pilot that handles order data, customer information, and billing details, this is a **non-negotiable security requirement**. Most enterprises will not send sensitive internal data to a third-party API (OpenAI, Anthropic, etc.).

Ollama runs on CPU (slower) or GPU (much faster). It supports models like Llama 3.1, Mistral, and Gemma that are competitive with GPT-3.5 for structured tasks like tool calling and RAG-based Q&A.

**Alternative considered:** OpenAI GPT-4 gives better raw reasoning quality, but the data privacy concern kills it for this use case in most enterprises. Self-hosted vLLM/TGI would work for larger deployments but adds infrastructure complexity.

**Tradeoff:** Smaller open-source models (7B-13B parameters) have less raw capability than GPT-4. For the structured tool-calling pattern in this application, they perform well — but the system should be designed to swap in a more capable model if one becomes available.
