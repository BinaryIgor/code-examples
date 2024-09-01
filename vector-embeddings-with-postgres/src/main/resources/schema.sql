CREATE TABLE IF NOT EXISTS vector_embedding_data (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    data JSONB NULL NULL
);
CREATE INDEX IF NOT EXISTS vector_embedding_data_type ON vector_embedding_data(type);