-- Seed Categories
INSERT INTO categories (name, description, parent_id) VALUES
('Grains', 'All types of food grains', NULL),
('Vegetables', 'Fresh farm vegetables', NULL),
('Fruits', 'Seasonal and all-weather fruits', NULL),
('Wheat', 'Various types of wheat grains', 1),
('Rice', 'Premium and daily use rice', 1);

-- Seed Products
INSERT INTO products (name, description, price, stock_quantity, category_id, farmer_id, location) VALUES
('Premium Sharbati Wheat', 'High quality Sharbati wheat directly from farm', 45.00, 500, 4, 1, 'Madhya Pradesh'),
('Basmati Rice Classic', 'Aged Basmati rice with long grains', 120.00, 200, 5, 2, 'Punjab'),
('Fresh Red Tomatoes', 'Organic farm fresh tomatoes', 40.00, 100, 2, 1, 'Maharashtra'),
('Organic Potatoes', 'Pesticide-free organic potatoes', 30.00, 300, 2, 3, 'Uttar Pradesh'),
('Alphonso Mangoes', 'Sweet and juicy Alphonso mangoes', 600.00, 50, 3, 2, 'Ratnagiri');
