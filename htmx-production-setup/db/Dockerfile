FROM postgres:16.2

COPY init_db.sql /docker-entrypoint-initdb.d/init_db.sql

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres