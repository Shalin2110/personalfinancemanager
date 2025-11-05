-- 1. Insert users
INSERT INTO user_central (username, password_hash, email) VALUES 
('john_doe', 'hashed_password_123', 'john@email.com');
INSERT INTO user_central (username, password_hash, email) VALUES 
('jane_smith', 'hashed_password_456', 'jane@email.com');

-- 2. Insert accounts
INSERT INTO account_central (user_id, name, currency, opening_balance) VALUES 
(1, 'Main Wallet', 'LKR', 50000.00);
INSERT INTO account_central (user_id, name, currency, opening_balance) VALUES 
(1, 'Savings Account', 'LKR', 150000.00);
INSERT INTO account_central (user_id, name, currency, opening_balance) VALUES 
(2, 'Primary Account', 'LKR', 75000.00);

-- 3. Insert categories
INSERT INTO category_central (user_id, name, type, parent_category_id) VALUES 
(1, 'Food and Dining', 'EXPENSE', NULL);
INSERT INTO category_central (user_id, name, type, parent_category_id) VALUES 
(1, 'Groceries', 'EXPENSE', 1);
INSERT INTO category_central (user_id, name, type, parent_category_id) VALUES 
(1, 'Salary', 'INCOME', NULL);
INSERT INTO category_central (user_id, name, type, parent_category_id) VALUES 
(2, 'Transportation', 'EXPENSE', NULL);
INSERT INTO category_central (user_id, name, type, parent_category_id) VALUES 
(2, 'Freelance Income', 'INCOME', NULL);

-- 4. Insert expenses with device_txn_id
INSERT INTO expense_central (device_txn_id, user_id, account_id, category_id, amount, expense_date, description) VALUES 
('TXN001', 1, 1, 2, 2500.00, DATE '2025-11-05', 'Weekly grocery shopping');
INSERT INTO expense_central (device_txn_id, user_id, account_id, category_id, amount, expense_date, description) VALUES 
('TXN002', 1, 1, 1, 1200.00, DATE '2025-11-08', 'Dinner with friends');
INSERT INTO expense_central (device_txn_id, user_id, account_id, category_id, amount, expense_date, description) VALUES 
('TXN003', 2, 3, 4, 800.00, DATE '2025-11-10', 'Bus fare to work');

-- 5. Insert budget (for November 2025)
INSERT INTO budget_central (user_id, category_id, start_date, end_date, amount) VALUES 
(1, 1, DATE '2025-11-01', DATE '2025-11-30', 15000.00);

-- 6. Insert savings goal
INSERT INTO savings_goal_central (user_id, name, target_amount, current_amount, start_date, target_date) VALUES 
(1, 'New Laptop', 200000.00, 50000.00, DATE '2025-11-01', DATE '2026-03-01');

-- 7. Insert sync logs for expenses (matching device_txn_id)
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('TXN001', 'expense', 'COMPLETED');
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('TXN002', 'expense', 'PENDING');
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('TXN003', 'expense', 'COMPLETED');

-- 8. Insert more expenses with device_txn_id (current and future dates)
INSERT INTO expense_central (device_txn_id, user_id, account_id, category_id, amount, expense_date, description, recurring_flag) VALUES 
('TXN004', 1, 2, 2, 3500.00, DATE '2025-11-12', 'Monthly grocery stock', 0);
INSERT INTO expense_central (device_txn_id, user_id, account_id, category_id, amount, expense_date, description, recurring_flag) VALUES 
('TXN005', 2, 3, 4, 1500.00, DATE '2025-11-15', 'Monthly transport pass', 1);

-- 9. Insert sync logs for the new expenses
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('TXN004', 'expense', 'FAILED');
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('TXN005', 'expense', 'PENDING');

-- 10. Insert sync logs for other table operations
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('USER001', 'user', 'COMPLETED');
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('ACCT001', 'account', 'COMPLETED');
INSERT INTO sync_log_central (device_txn_id, table_name, status) VALUES 
('CAT001', 'category', 'PENDING');

COMMIT;