CREATE DATABASE test;

CONNECT test;

CREATE TABLE table_few_indexes (
  id BIGINT AUTO_INCREMENT,
  name VARCHAR(255) UNIQUE NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version BIGINT NOT NULL,
  PRIMARY KEY(id)
);
CREATE INDEX table_few_indexes_created_at ON table_few_indexes(created_at);
CREATE INDEX table_few_indexes_updated_at ON table_few_indexes(updated_at);
--CREATE INDEX table_few_indexes_status ON table_few_indexes(status);
--CREATE INDEX table_few_indexes_version ON table_few_indexes(version);

CREATE TABLE table_single_index (
  id BIGINT AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  version BIGINT NOT NULL,
  PRIMARY KEY(id)
);