CREATE TABLE IF NOT EXISTS user
(
    id TEXT NOT NULL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    name TEXT
);

CREATE TABLE IF NOT EXISTS project
(
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    owner_id TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS project_owner_id ON project(owner_id);

CREATE TABLE IF NOT EXISTS task
(
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    project_id TEXT NOT NULL,
    status TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES project(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS task_project_id ON task(project_id);