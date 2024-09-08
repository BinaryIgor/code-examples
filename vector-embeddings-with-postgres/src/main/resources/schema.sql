CREATE TABLE IF NOT EXISTS vembedding_data (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    data JSONB NULL NULL
);
CREATE INDEX IF NOT EXISTS vembedding_data_type ON vembedding_data(type);