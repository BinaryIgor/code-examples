CREATE DATABASE test;

\c test;

CREATE TABLE table_few_indexes (
  id BIGSERIAL PRIMARY KEY,
  name TEXT UNIQUE NOT NULL,
  status TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version BIGINT NOT NULL
);
CREATE INDEX table_few_indexes_created_at ON table_few_indexes(created_at);
CREATE INDEX table_few_indexes_updated_at ON table_few_indexes(updated_at);
-- CREATE INDEX table_few_indexes_status ON table_few_indexes(status);
-- CREATE INDEX table_few_indexes_version ON table_few_indexes(version);

CREATE TABLE table_single_index (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version BIGINT NOT NULL
);