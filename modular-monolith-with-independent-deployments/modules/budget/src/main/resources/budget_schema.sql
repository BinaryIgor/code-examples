CREATE TABLE IF NOT EXISTS budget (
    id UUID PRIMARY KEY,
    amount NUMERIC(4, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);