-- Password is 'password123' for all users
-- Hash: $2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIade0/J.l.u8y89BEeezfe

-- 1. Create Users
INSERT INTO users (first_name, last_name, email, password_hash, profile_type, is_verified, phone)
VALUES ('John', 'Pro', 'pro@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIade0/J.l.u8y89BEeezfe',
        'INVESTOR_PRIVATE', true, '0601010101'),
       ('Jane', 'Standard', 'standard@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIade0/J.l.u8y89BEeezfe',
        'INVESTOR_PRIVATE', true, '0602020202'),
       ('Bob', 'Free', 'free@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.My7GCIade0/J.l.u8y89BEeezfe',
        'INVESTOR_PRIVATE', true, '0603030303');

-- 2. Assign Roles (Linked to users by sub-selects to avoid hardcoded IDs)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER_PRO'
FROM users
WHERE email = 'pro@example.com';

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER_STANDARD'
FROM users
WHERE email = 'standard@example.com';

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER_FREE'
FROM users
WHERE email = 'free@example.com';

-- 3. Create Subscriptions
-- PRO User
INSERT INTO user_subscriptions (user_id, plan_id, start_date, end_date, status, auto_renew)
SELECT (SELECT id FROM users WHERE email = 'pro@example.com'),
       (SELECT id FROM subscription_plans WHERE name = 'PRO'),
       CURRENT_DATE,
       CURRENT_DATE + INTERVAL '1 year',
       'ACTIVE',
       true;

-- STANDARD User
INSERT INTO user_subscriptions (user_id, plan_id, start_date, end_date, status, auto_renew)
SELECT (SELECT id FROM users WHERE email = 'standard@example.com'),
       (SELECT id FROM subscription_plans WHERE name = 'STANDARD'),
       CURRENT_DATE,
       CURRENT_DATE + INTERVAL '1 year',
       'ACTIVE',
       true;

-- FREE User
INSERT INTO user_subscriptions (user_id, plan_id, start_date, end_date, status, auto_renew)
SELECT (SELECT id FROM users WHERE email = 'free@example.com'),
       (SELECT id FROM subscription_plans WHERE name = 'FREE'),
       CURRENT_DATE,
       NULL,
       'ACTIVE',
       false;
