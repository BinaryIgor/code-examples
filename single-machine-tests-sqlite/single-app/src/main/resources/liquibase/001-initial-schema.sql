-- multiple statements in a query not supported: https://github.com/xerial/sqlite-jdbc/issues/277 --
CREATE TABLE account (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    created_at INTEGER NOT NULL,
    version INTEGER NOT NULL
);