CREATE TABLE IF NOT EXISTS campaign (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    budget_id UUID NOT NULL,
    inventory_id UUID NOT NULL,
    start_date DATE,
    end_date DATE
);