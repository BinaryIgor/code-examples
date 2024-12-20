-- multiple statements in a query not supported: https://github.com/xerial/sqlite-jdbc/issues/277 --
CREATE TABLE account (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL
);