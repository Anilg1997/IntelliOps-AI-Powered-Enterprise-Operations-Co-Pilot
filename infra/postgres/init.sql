-- IntelliOps - PostgreSQL initialization
-- Creates the pgvector extension needed for AI RAG (Phase 3+)
-- Creates both intellops_order and intellops_auth databases

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

-- Create auth database if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'intellops_auth') THEN
        CREATE DATABASE intellops_auth;
    END IF;
END
$$;

-- The order_service database and schema are managed by Flyway migrations
-- This file ensures the pgvector extension is available for future phases
