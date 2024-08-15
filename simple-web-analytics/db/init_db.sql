CREATE DATABASE analytics;

CREATE USER analytics WITH password 'analytics';
ALTER DATABASE analytics OWNER TO analytics;

\c analytics analytics;

CREATE TABLE analytics_event (
    timestamp TIMESTAMP NOT NULL,
    ip TEXT NOT NULL,
    device_id UUID NOT NULL,
    user_id UUID,
    url TEXT NOT NULL,
    browser TEXT NOT NULL,
    os TEXT NOT NULL,
    device TEXT NOT NULL,
    type TEXT NOT NULL,
    data JSONB
);
CREATE INDEX analytics_event_timestamp ON analytics_event(timestamp);
CREATE INDEX analytics_event_type_timestamp ON analytics_event(type, timestamp);