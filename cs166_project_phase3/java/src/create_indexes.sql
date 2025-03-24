-- Drop existing indexes if they exist (to avoid duplication issues)
DROP INDEX IF EXISTS idx_users_login;
DROP INDEX IF EXISTS idx_foodorder_orderID;
DROP INDEX IF EXISTS idx_foodorder_login;
DROP INDEX IF EXISTS idx_itemsinorder_orderID;
DROP INDEX IF EXISTS idx_itemsinorder_itemName;
DROP INDEX IF EXISTS idx_items_itemName;
DROP INDEX IF EXISTS idx_store_storeID;

-- Create optimized indexes

-- Optimize user login lookup (used for authentication and order history)
CREATE INDEX idx_users_login ON Users(login);

-- Optimize order lookups by orderID
CREATE INDEX idx_foodorder_orderID ON FoodOrder(orderID);

-- Optimize customer order history lookups
CREATE INDEX idx_foodorder_login ON FoodOrder(login);

-- Optimize item lookups within a specific order
CREATE INDEX idx_itemsinorder_orderID ON ItemsInOrder(orderID);

-- Optimize item lookup when retrieving order details
CREATE INDEX idx_itemsinorder_itemName ON ItemsInOrder(itemName);

-- Optimize menu browsing and order-related item queries
CREATE INDEX idx_items_itemName ON Items(itemName);

-- Optimize store-related lookups in orders
CREATE INDEX idx_store_storeID ON Store(storeID);
