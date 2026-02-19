CREATE TABLE accounts (data JSONB NOT NULL);
CREATE UNIQUE INDEX accounts_id ON accounts ((data->>'id'));
CREATE INDEX accounts_created_at_idx ON accounts ((data->>'createdAt'));
CREATE INDEX accounts_owners_idx ON accounts USING GIN ((data->'owners'));

CREATE TABLE products (data JSONB NOT NULL);
CREATE UNIQUE INDEX products_id ON products ((data->>'id'));
CREATE UNIQUE INDEX products_name_unique_idx ON products ((data->>'name'));
CREATE INDEX products_categories_idx ON products USING GIN ((data->'categories'));
CREATE INDEX products_tags_idx ON products USING GIN ((data->'tags'));
CREATE INDEX products_created_at_idx ON products ((data->>'createdAt'));