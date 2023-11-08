CREATE DATABASE campaign;
CREATE DATABASE budget;
CREATE DATABASE inventory;

CREATE USER campaign_module WITH password 'campaign_module';
GRANT ALL PRIVILEGES ON DATABASE campaign TO campaign_module;
\c campaign;
GRANT ALL ON SCHEMA public TO campaign_module;

CREATE USER budget_module WITH password 'budget_module';
GRANT ALL PRIVILEGES ON DATABASE budget TO budget_module;
\c budget;
GRANT ALL ON SCHEMA public TO budget_module;

CREATE USER inventory_module WITH password 'inventory_module';
GRANT ALL PRIVILEGES ON DATABASE inventory TO inventory_module;
\c inventory;
GRANT ALL ON SCHEMA public TO inventory_module;