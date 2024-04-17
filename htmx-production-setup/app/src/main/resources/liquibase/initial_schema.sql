CREATE SCHEMA "user";

CREATE TABLE "user"."user" (
    id UUID PRIMARY KEY,
    email TEXT UNIQUE,
    name TEXT NOT NULL,
    password TEXT NOT NULL,
    language TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE SCHEMA day;

CREATE TABLE day.day (
    user_id UUID NOT NULL,
    date DATE NOT NULL,
    description TEXT,

    PRIMARY KEY(user_id, date)
);

