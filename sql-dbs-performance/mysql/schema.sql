CREATE TABLE `user` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE `order` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES user(id) ON DELETE CASCADE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);
CREATE INDEX order_user_id ON `order`(user_id);

CREATE TABLE `item` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  price NUMERIC(10, 2) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE TABLE `order_item` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES `order`(id) ON DELETE CASCADE,
  item_id BIGINT NOT NULL REFERENCES `item`(id) ON DELETE CASCADE
);
CREATE INDEX order_item_order_id ON `order_item`(order_id);
CREATE INDEX order_item_item_id ON `order_item`(item_id);