CREATE DATABASE htmx_db;

CREATE USER htmx_app WITH password 'htmx_db_password';
ALTER DATABASE htmx_db OWNER TO htmx_app;