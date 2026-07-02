CREATE TABLE account (
  -- Sadly, still no native UUID type in MySQL! --
  id CHAR(36) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL
);