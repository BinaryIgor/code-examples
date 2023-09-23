CREATE TABLE account (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    country_code INTEGER NOT NULL,
    attributes JSONB NOT NULL
);
-- CREATE INDEX account_name ON account (name);
CREATE INDEX account_attributes ON account USING GIN (attributes);