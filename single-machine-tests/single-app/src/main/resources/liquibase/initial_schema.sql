CREATE TABLE account (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL
);

CREATE INDEX account_name ON account(name);