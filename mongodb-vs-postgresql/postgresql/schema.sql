CREATE TABLE accounts (data JSONB NOT NULL);
CREATE INDEX idx_accounts_id ON accounts ((data->>'id'));
CREATE INDEX idx_accounts_created_at ON accounts ((data->>'created_at'));

CREATE TABLE products (data JSONB NOT NULL);
CREATE INDEX idx_products_id ON products ((data->>'id'));
CREATE UNIQUE INDEX unique_idx_products_name ON products ((data->>'name'));
CREATE INDEX idx_products_categories ON products USING GIN ((data->'categories'));
CREATE INDEX idx_products_tags ON products USING GIN ((data->'tags'));
CREATE INDEX idx_products_created_at ON products ((data->>'created_at'));