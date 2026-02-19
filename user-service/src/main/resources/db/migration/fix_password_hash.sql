-- ============================================
-- Password Hash Fix for User Service
-- ============================================
-- This script updates the password hash for seeded users
-- Password: "password"
-- BCrypt Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Step 1: Check current passwords
SELECT mobile_number, full_name, password 
FROM users 
WHERE mobile_number IN ('9988776655', '9876543210', '1122334455');

-- Step 2: Update passwords to correct BCrypt hash
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE mobile_number IN ('9988776655', '9876543210', '1122334455');

-- Step 3: Verify update
SELECT mobile_number, full_name, 
       CASE 
           WHEN password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
           THEN '✅ CORRECT' 
           ELSE '❌ WRONG' 
       END as password_status
FROM users 
WHERE mobile_number IN ('9988776655', '9876543210', '1122334455');

-- ============================================
-- IMPORTANT: The hash $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- is a VALID BCrypt hash for the password "password"
-- ============================================
