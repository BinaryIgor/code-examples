db.createCollection("accounts");
db.accounts.createIndex(
  { createdAt: 1 },
  { name: "accounts_created_at_idx" }
);
db.accounts.createIndex(
  { owners: 1 },
  { name: "accounts_owners_idx" }
);

db.createCollection("products");
db.products.createIndex(
  { name: 1 },
  {
    name: "products_name_unique_idx",
    unique: true
  }
);
db.products.createIndex(
  { categories: 1 },
  { name: "products_categories_idx" }
);
db.products.createIndex(
  { tags: 1 },
  { name: "products_tags_idx" }
);
db.products.createIndex(
  { createdAt: 1 },
  { name: "products_created_at_idx" }
);
