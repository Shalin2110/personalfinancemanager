-- USER TABLE
INSERT INTO user_central (username, password_hash, email)
VALUES 
('sunil', 'hash123', 'sunil@example.com');

INSERT INTO user_central (username, password_hash, email)
VALUES 
('nadun', 'hash456', 'nadun@example.com');

-- ACCOUNT TABLE
INSERT INTO account_central (user_id, name, currency, opening_balance)
VALUES 
(1, 'Main Wallet', 'LKR', 25000.00);

INSERT INTO account_central (user_id, name, currency, opening_balance)
VALUES 
(2, 'Savings Account', 'LKR', 50000.00);

-- CATEGORY TABLE
INSERT INTO category_central (user_id, name, type)
VALUES 
(1, 'Food and Drinks', 'EXPENSE');

INSERT INTO category_central (user_id, name, type)
VALUES 
(1, 'Salary', 'INCOME');

-- EXPENSE TABLE
INSERT INTO expense_central (device_txn_id, user_id, account_id, category_id, amount, expense_date, description)
VALUES 
('TXN001', 1, 1, 1, 3500.00, DATE '2025-10-01', 'Groceries and lunch');

INSERT INTO expense_central (device_txn_id, user_id, account_id, category_id, amount, expense_date, description)
VALUES 
('TXN002', 1, 1, 1, 1200.00, DATE '2025-10-05', 'Dinner with friends');

-- BUDGET TABLE
INSERT INTO budget_central (user_id, category_id, start_date, end_date, amount, alert_threshold_pct)
VALUES 
(1, 1, DATE '2025-10-01', DATE '2025-10-31', 10000.00, 80);

INSERT INTO budget_central (user_id, category_id, start_date, end_date, amount, alert_threshold_pct)
VALUES 
(1, 1, DATE '2025-09-01', DATE '2025-09-30', 9000.00, 75);

-- SAVINGS GOAL TABLE
INSERT INTO savings_goal_central (user_id, name, target_amount, current_amount, start_date, target_date)
VALUES 
(1, 'New Laptop', 150000.00, 50000.00, DATE '2025-08-01', DATE '2026-02-01');

INSERT INTO savings_goal_central (user_id, name, target_amount, current_amount, start_date, target_date)
VALUES 
(1, 'Vacation Fund', 100000.00, 45000.00, DATE '2025-07-15', DATE '2026-01-15');

-- SYNC LOG TABLE
INSERT INTO sync_log_central (device_txn_id, table_name, status, retries)
VALUES 
('TXN001', 'expense', 'SUCCESS', 1);

INSERT INTO sync_log_central (device_txn_id, table_name, status, retries)
VALUES 
('TXN002', 'budget', 'FAILED', 2);
