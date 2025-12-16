INSERT INTO subscription_plans (name, price_monthly, price_yearly, max_properties, features)
VALUES ('FREE', 0.00, 0.00, 2, '{"modules": ["DASHBOARD", "PROPERTIES_BASIC", "DOCUMENTS_LIMITED"]}'),
       ('STANDARD', 19.99, 199.99, 10,
        '{"modules": ["DASHBOARD", "PROPERTIES_FULL", "CONSTRUCTION", "FINANCE_BASIC", "DOCUMENTS_FULL"]}'),
       ('PRO', 49.99, 499.99, 9999, '{"modules": ["ALL", "MULTI_USER", "OCR", "API", "MARKETPLACE"]}');
