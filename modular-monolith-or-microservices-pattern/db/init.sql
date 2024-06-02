CREATE DATABASE "user";
CREATE DATABASE project;

CREATE USER user_module WITH password 'user_module';
ALTER DATABASE "user" OWNER TO user_module;

CREATE USER project_module WITH password 'project_module';
ALTER DATABASE project OWNER TO project_module;
