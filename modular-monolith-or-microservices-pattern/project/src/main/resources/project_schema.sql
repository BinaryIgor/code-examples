CREATE TABLE IF NOT EXISTS project (
    id UUID PRIMARY KEY,
    namespace TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX project_namespace ON project(namespace);

CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL
);

CREATE TABLE project_user (
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,

    PRIMARY KEY(project_id, user_id)
);
CREATE INDEX project_user_user_id ON project_user(user_id);

-- TODO: is it needed? --
CREATE TABLE IF NOT EXISTS outbox_message (
    id UUID PRIMARY KEY,
    message JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);