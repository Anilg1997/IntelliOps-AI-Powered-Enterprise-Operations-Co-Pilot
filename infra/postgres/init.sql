-- IntelliOps - PostgreSQL initialization
-- Creates the pgvector extension needed for AI RAG (Phase 3+)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- The order_service database and schema are managed by Flyway migrations
-- This file ensures the pgvector extension is available for future phases
