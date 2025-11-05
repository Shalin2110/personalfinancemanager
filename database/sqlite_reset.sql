-- Delete all records from all tables (order matters due to foreign keys)
DELETE FROM sync_log;
DELETE FROM expense;
DELETE FROM budget;
DELETE FROM savings_goal;
DELETE FROM account;
DELETE FROM category;
DELETE FROM user;

-- Reset auto-increment sequences
DELETE FROM sqlite_sequence WHERE name IN ('user', 'account', 'category', 'expense', 'budget', 'savings_goal', 'sync_log');