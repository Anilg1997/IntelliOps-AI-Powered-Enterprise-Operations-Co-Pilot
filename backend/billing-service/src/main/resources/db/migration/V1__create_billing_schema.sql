-- Phase 4: Legacy Billing Schema (Oracle-compatible via H2 Oracle mode)
-- Creates tables for billing accounts, invoices, and payments
-- simulating a legacy enterprise billing system.

CREATE TABLE IF NOT EXISTS billing_accounts (
    id              NUMBER PRIMARY KEY,
    account_number  VARCHAR2(50)  NOT NULL UNIQUE,
    customer_email  VARCHAR2(255) NOT NULL,
    customer_name   VARCHAR2(255) NOT NULL,
    status          VARCHAR2(20)  NOT NULL DEFAULT 'ACTIVE',
    balance         NUMBER(14,2)  NOT NULL DEFAULT 0,
    credit_limit    NUMBER(14,2)  DEFAULT 50000,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoices (
    id              NUMBER PRIMARY KEY,
    invoice_number  VARCHAR2(50)  NOT NULL UNIQUE,
    order_number    VARCHAR2(50)  NOT NULL,
    account_id      NUMBER        NOT NULL,
    status          VARCHAR2(20)  NOT NULL DEFAULT 'PENDING',
    amount          NUMBER(14,2)  NOT NULL,
    paid_amount     NUMBER(14,2)  DEFAULT 0,
    due_date        DATE          NOT NULL,
    paid_date       DATE,
    description     VARCHAR2(500),
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP,
    CONSTRAINT fk_invoice_account FOREIGN KEY (account_id) REFERENCES billing_accounts(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id              NUMBER PRIMARY KEY,
    payment_ref     VARCHAR2(50)  NOT NULL UNIQUE,
    invoice_number  VARCHAR2(50)  NOT NULL,
    account_id      NUMBER        NOT NULL,
    amount          NUMBER(14,2)  NOT NULL,
    payment_method  VARCHAR2(50)  NOT NULL,
    transaction_id  VARCHAR2(100),
    status          VARCHAR2(20)  NOT NULL DEFAULT 'COMPLETED',
    notes           VARCHAR2(500),
    created_at      TIMESTAMP     NOT NULL,
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES billing_accounts(id)
);

CREATE SEQUENCE IF NOT EXISTS billing_accounts_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS invoices_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS payments_seq START WITH 1 INCREMENT BY 1;
