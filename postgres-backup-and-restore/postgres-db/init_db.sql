CREATE DATABASE backup_db;

CREATE USER backup WITH password 'backup';
GRANT ALL PRIVILEGES ON DATABASE backup_db TO backup;
\c backup_db;
GRANT ALL ON SCHEMA public TO backup;