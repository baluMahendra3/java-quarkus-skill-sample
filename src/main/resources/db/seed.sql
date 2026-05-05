-- Travel Business Management DB Setup
-- Run this in PostgreSQL to create the database and user

-- CREATE DATABASE travel_db;
-- CREATE USER travel_user WITH ENCRYPTED PASSWORD 'travel_pass';
-- GRANT ALL PRIVILEGES ON DATABASE travel_db TO travel_user;

-- Seed: Default admin user
-- Password hash below = bcrypt of 'Admin@123'
INSERT INTO users (name, email, password, role, created_at, active)
VALUES (
    'System Admin',
    'admin@travel.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    NOW(),
    true
) ON CONFLICT (email) DO NOTHING;

-- Seed: Sample Manager
INSERT INTO users (name, email, password, role, created_at, active)
VALUES (
    'Manager One',
    'manager@travel.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'MANAGER',
    NOW(),
    true
) ON CONFLICT (email) DO NOTHING;

-- Seed: Sample Driver
INSERT INTO drivers (name, phone, license_number, vehicle_number, vehicle_type, status, joined_date, created_at)
VALUES (
    'Ravi Kumar',
    '9876543210',
    'TN-1234-5678',
    'TN 01 AB 1234',
    'Sedan',
    'ACTIVE',
    CURRENT_DATE,
    NOW()
) ON CONFLICT (license_number) DO NOTHING;
