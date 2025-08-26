-- Initial schema for orders query database
-- This creates the read model table for CQRS architecture

CREATE TABLE orders
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id  BIGINT         NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    created_at   TIMESTAMP      NOT NULL
);

-- Create index on customer_id for efficient queries
CREATE INDEX idx_orders_customer_id ON orders (customer_id);

-- Create index on status for efficient filtering
CREATE INDEX idx_orders_status ON orders (status);

-- Create index on created_at for time-based queries
CREATE INDEX idx_orders_created_at ON orders (created_at);