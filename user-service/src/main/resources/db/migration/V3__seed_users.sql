-- Password is 'password' (BCrypt Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- 1. Insert Users
INSERT INTO users (mobile_number, email, password, full_name, village, district, state, pincode, is_active, is_verified) 
VALUES 
('9988776655', 'farmer@rural.com', '$2a$10$7KnGWvkhI6rAeyQO./4Xw.62OJ76atBMT5frgyOuxy7PZ1p0wOjZm', 'Ramesh Farmer', 'Kisanpur', 'Pune', 'Maharashtra', '412306', true, true),
('9876543210', 'buyer@city.com', '$2a$10$7KnGWvkhI6rAeyQO./4Xw.62OJ76atBMT5frgyOuxy7PZ1p0wOjZm', 'Suresh Buyer', 'Shivajinagar', 'Pune', 'Maharashtra', '411005', true, true),
('1122334455', 'admin@rural.com', '$2a$10$7KnGWvkhI6rAeyQO./4Xw.62OJ76atBMT5frgyOuxy7PZ1p0wOjZm', 'System Admin', 'Headquarters', 'Mumbai', 'Maharashtra', '400001', true, true)
ON CONFLICT (mobile_number) DO NOTHING;

-- 2. Link Users to Roles
-- Farmer (ID might vary, so subquery)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.mobile_number = '9988776655' AND r.name = 'FARMER'
ON CONFLICT DO NOTHING;

-- Buyer
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.mobile_number = '9876543210' AND r.name = 'BUYER'
ON CONFLICT DO NOTHING;

-- Admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.mobile_number = '1122334455' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
