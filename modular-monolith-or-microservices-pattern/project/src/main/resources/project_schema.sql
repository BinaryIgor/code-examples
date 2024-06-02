CREATE TABLE IF NOT EXISTS project (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS project_user (
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,

    PRIMARY KEY(project_id, user_id)
);
CREATE INDEX IF NOT EXISTS project_user_user_id ON project_user(user_id);