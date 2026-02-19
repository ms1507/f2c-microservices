-- Seed Roles
INSERT INTO roles (name, description) VALUES 
('FARMER', 'Farmers who list products'),
('BUYER', 'Buyers who purchase products'),
('SHOP_OWNER', 'Shop owners who manage local inventory'),
('ADMIN', 'System Administrators')
ON CONFLICT (name) DO NOTHING;

-- Seed Permissions (Optional start)
INSERT INTO permissions (name, description) VALUES
('PRODUCT_CREATE', 'Can create a new product'),
('PRODUCT_READ', 'Can view products'),
('ORDER_PLACE', 'Can place an order')
ON CONFLICT (name) DO NOTHING;

-- Map Roles to Permissions (Basic Setup)
-- Farmer can Create Products
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'FARMER' AND p.name = 'PRODUCT_CREATE'
ON CONFLICT DO NOTHING;
