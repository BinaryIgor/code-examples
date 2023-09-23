FROM postgres:15.3

COPY schema.sql /docker-entrypoint-initdb.d/schema.sql

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres