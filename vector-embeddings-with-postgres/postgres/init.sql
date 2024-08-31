CREATE DATABASE vembeddings;
CREATE USER vembeddings WITH password 'vembeddings';
ALTER DATABASE vembeddings OWNER TO vembeddings;
\c vembeddings;
CREATE EXTENSION vector;