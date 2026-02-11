db.createCollection("accounts");
db.accounts.createIndex(
  { createdAt: 1 },
  { name: "idx_accounts_created_at"}
);

db.createCollection("products");
db.products.createIndex(
  { name: 1 },
  {
    name: "unique_idx_products_name",
    unique: true
  }
);
db.products.createIndex(
  { categories: 1 },
  { name: "idx_products_categories" }
);
db.products.createIndex(
  { tags: 1 },
  { name: "idx_products_tags" }
);
db.products.createIndex(
  { createdAt: 1 },
  { name: "idx_products_created_at" }
);
