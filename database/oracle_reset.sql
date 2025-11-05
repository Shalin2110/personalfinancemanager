-- First delete all data
DELETE FROM sync_log_central;
DELETE FROM expense_central;
DELETE FROM budget_central;
DELETE FROM savings_goal_central;
DELETE FROM account_central;
DELETE FROM category_central;
DELETE FROM user_central;

-- Manually reset each identity column
ALTER TABLE user_central MODIFY (user_id GENERATED AS IDENTITY (START WITH 1));
ALTER TABLE account_central MODIFY (account_id GENERATED AS IDENTITY (START WITH 1));
ALTER TABLE category_central MODIFY (category_id GENERATED AS IDENTITY (START WITH 1));
ALTER TABLE expense_central MODIFY (expense_id GENERATED AS IDENTITY (START WITH 1));
ALTER TABLE budget_central MODIFY (budget_id GENERATED AS IDENTITY (START WITH 1));
ALTER TABLE savings_goal_central MODIFY (goal_id GENERATED AS IDENTITY (START WITH 1));
ALTER TABLE sync_log_central MODIFY (sync_id GENERATED AS IDENTITY (START WITH 1));

COMMIT;