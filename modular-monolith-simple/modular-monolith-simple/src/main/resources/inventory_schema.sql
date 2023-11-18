CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE IF NOT EXISTS inventory.inventory (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory.inventory_sku (
    inventory_id UUID NOT NULL REFERENCES inventory.inventory ON DELETE CASCADE,
    sku TEXT NOT NULL,

    PRIMARY KEY(inventory_id, sku)
);