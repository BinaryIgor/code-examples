FROM postgres:15.3

COPY init_db.sql /docker-entrypoint-initdb.d/init_db.sql

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres