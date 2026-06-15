# IntelliOps Architecture

## System Architecture

```
                        ┌─────────────────────────┐
                        │   Angular Frontend       │
                        │  (Co-pilot chat + admin  │
                        │   dashboards)            │
                        └───────────┬──────────────┘
                                     │ GraphQL (BFF) + REST + SSE/WebSocket
                                     ▼
        ┌─────────────────────────────────────────────────────┐
        │              AI Co-Pilot Service                     │
        │   Spring AI + LangChain4j + Ollama (local LLM)       │
        │   - RAG over runbooks/FAQs (pgvector)                │
        │   - Agent w/ tool calling                            │
        │   - Conversation memory (MongoDB)                    │
        └───────┬───────────────┬───────────────┬──────────────┘
                │ MCP            │ MCP           │ MCP
                ▼                ▼               ▼
     ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────────┐
     │ Order Service     │ │ Inventory/Catalog │ │ Legacy Billing       │
     │ Spring Boot       │ │ Spring Boot       │ │ Adapter Service       │
     │ PostgreSQL        │ │ MongoDB           │ │ Oracle DB             │
     │ REST + GraphQL    │ │ gRPC (internal)   │ │ SOAP (legacy contract)│
     └────────┬──────────┘ └────────┬──────────┘ └───────────┬──────────┘
              │ Kafka events                                 │
              ▼                                               │
     ┌──────────────────────┐                                 │
     │ Notification/Activity│◄────────────────────────────────┘
     │ Service (Kafka)      │
     └──────────────────────┘
```

## Sequence Diagram: End-to-End AI Co-Pilot Query

```
User                           AI Service          RAG (pgvector)    Order Service    Inventory(gRPC)   Billing(SOAP)
  │                                │                    │                  │                  │               │
  │ "Why is order #4521 stuck,     │                    │                  │                  │               │
  │  and has customer been billed?"│                    │                  │                  │               │
  │───────────────────────────────►│                    │                  │                  │               │
  │                                │                    │                  │                  │               │
  │                         1. Retrieve RAG context     │                  │                  │               │
  │                                │───────────────────►│                  │                  │               │
  │                                │◄───────────────────│                  │                  │               │
  │                                │ (runbook: "Order Status Codes")        │                  │               │
  │                                │                    │                  │                  │               │
  │                         2. Call order-mcp           │                  │                  │               │
  │                                │──────────────────────────────────────►│                  │               │
  │                                │◄──────────────────────────────────────│                  │               │
  │                                │ (status=ON_HOLD, reason=STOCK_HOLD)   │                  │               │
  │                                │                    │                  │                  │               │
  │                         3. Call inventory-mcp       │                  │                  │               │
  │                                │────────────────────────────────────────────────────────►│               │
  │                                │◄────────────────────────────────────────────────────────│               │
  │                                │ (stock=0, restock ETA=5 days)         │                  │               │
  │                                │                    │                  │                  │               │
  │                         4. Call billing-mcp         │                  │                  │               │
  │                                │──────────────────────────────────────────────────────────────────────►│
  │                                │◄──────────────────────────────────────────────────────────────────────│
  │                                │ (invoice=PENDING, not charged)        │                  │               │
  │                                │                    │                  │                  │               │
  │                         5. LLM synthesizes answer   │                  │                  │               │
  │◄───────────────────────────────│                    │                  │                  │               │
  │ "Order #4521 is ON_HOLD due    │                    │                  │                  │               │
  │  to stock outage. Customer     │                    │                  │                  │               │
  │  has NOT been billed. Action:  │                    │                  │                  │               │
  │  Notify customer of delay."    │                    │                  │                  │               │
```

## Service Communication Matrix

| From \ To | Order Service | Inventory | Billing | AI Co-Pilot |
|-----------|--------------|-----------|---------|-------------|
| **Order Service** | — | gRPC (sync check/reserve) | — | Kafka (events) |
| **Inventory** | — | — | — | MCP (tool call) |
| **Billing** | — | — | — | MCP (tool call) |
| **AI Co-Pilot** | MCP (tool call) | MCP (tool call) | MCP (tool call) | — |
| **Frontend** | GraphQL (BFF) | — | — | SSE (stream) |
| **Notif. Service** | — | — | — | Kafka (consume) |

## Technology Decision Log

| Decision | Rationale | Tradeoffs |
|----------|-----------|-----------|
| PostgreSQL for Order Service | ACID transactions needed for financial data | Fixed schema; less flexible for varying product attributes |
| MongoDB for Inventory | Flexible document model for varying product specs | No built-in joins; eventual consistency |
| GraphQL as BFF | Single round-trip for complex order+customer+items queries | Additional complexity vs REST; query depth management |
| gRPC for stock check | Low-latency binary protocol for hot-path calls | Tight coupling on protobuf contracts |
| SOAP for legacy billing | Simulates real-world enterprise legacy integration | Heavy protocol; not suitable for new greenfield services |
| Kafka for events | Decouples services; creates audit trail for AI agent | Operational complexity; at-least-once semantics |
| Ollama for AI | Runs locally; no data leaves the network | Smaller models than GPT-4; needs local resources |
