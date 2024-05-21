FROM postgres:16

COPY init.sql /docker-entrypoint-initdb.d/init.sql

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres