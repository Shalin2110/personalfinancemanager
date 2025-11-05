// User Sync Procedure
CREATE OR REPLACE PROCEDURE proc_sync_user (
    p_user_id     IN NUMBER,
    p_username    IN VARCHAR2,
    p_password_hash IN VARCHAR2,
    p_email       IN VARCHAR2,
    p_created_at  IN TIMESTAMP,
    p_delete_flag IN NUMBER
)
AS
BEGIN
    IF p_delete_flag = 1 THEN
        UPDATE user_central
        SET delete_flag = 1
        WHERE user_id = p_user_id;
    ELSE
        MERGE INTO user_central t
        USING (SELECT p_user_id AS user_id FROM dual) s
        ON (t.user_id = s.user_id)
        WHEN MATCHED THEN
            UPDATE SET
                t.username     = p_username,
                t.password_hash= p_password_hash,
                t.email        = p_email,
                t.created_at   = NVL(p_created_at, t.created_at),
                t.delete_flag  = 0
        WHEN NOT MATCHED THEN
            INSERT (user_id, username, password_hash, email, created_at, delete_flag)
            VALUES (p_user_id, p_username, p_password_hash, p_email, p_created_at, 0);
    END IF;
    COMMIT;
END proc_sync_user;
/

// Account Sync Procedure
CREATE OR REPLACE PROCEDURE proc_sync_account (
    p_account_id     IN NUMBER,
    p_user_id        IN NUMBER,
    p_name           IN VARCHAR2,
    p_currency       IN VARCHAR2,
    p_opening_balance IN NUMBER,
    p_delete_flag    IN NUMBER
)
AS
BEGIN
    IF p_delete_flag = 1 THEN
        UPDATE account_central
        SET delete_flag = 1
        WHERE account_id = p_account_id;
    ELSE
        MERGE INTO account_central t
        USING (SELECT p_account_id AS account_id FROM dual) s
        ON (t.account_id = s.account_id)
        WHEN MATCHED THEN
            UPDATE SET
                t.user_id         = p_user_id,
                t.name            = p_name,
                t.currency        = p_currency,
                t.opening_balance = NVL(p_opening_balance, t.opening_balance),
                t.delete_flag     = 0
        WHEN NOT MATCHED THEN
            INSERT (account_id, user_id, name, currency, opening_balance, delete_flag)
            VALUES (p_account_id, p_user_id, p_name, p_currency, p_opening_balance, 0);
    END IF;
    COMMIT;
END proc_sync_account;
/

// Budget Sync Procedure
CREATE OR REPLACE PROCEDURE proc_sync_budget (
    p_budget_id    IN NUMBER,
    p_user_id      IN NUMBER,
    p_category_id  IN NUMBER,
    p_start_date   IN DATE,
    p_end_date     IN DATE,
    p_amount       IN NUMBER,
    p_alert_pct    IN NUMBER,
    p_delete_flag  IN NUMBER
)
AS
BEGIN
    IF p_delete_flag = 1 THEN
        UPDATE budget_central
        SET delete_flag = 1
        WHERE budget_id = p_budget_id;
    ELSE
        MERGE INTO budget_central t
        USING (SELECT p_budget_id AS budget_id FROM dual) s
        ON (t.budget_id = s.budget_id)
        WHEN MATCHED THEN
            UPDATE SET
                t.user_id          = p_user_id,
                t.category_id      = p_category_id,
                t.start_date       = NVL(p_start_date, t.start_date),
                t.end_date         = NVL(p_end_date, t.end_date),
                t.amount           = p_amount,
                t.alert_threshold_pct = NVL(p_alert_pct, t.alert_threshold_pct),
                t.delete_flag      = 0
        WHEN NOT MATCHED THEN
            INSERT (budget_id, user_id, category_id, start_date, end_date, amount, alert_threshold_pct, delete_flag)
            VALUES (p_budget_id, p_user_id, p_category_id, p_start_date, p_end_date, p_amount, p_alert_pct, 0);
    END IF;
    COMMIT;
END proc_sync_budget;
/

// Category Sync Procedure
CREATE OR REPLACE PROCEDURE proc_sync_category (
    p_category_id       IN NUMBER,
    p_user_id           IN NUMBER,
    p_name              IN VARCHAR2,
    p_type              IN VARCHAR2,
    p_parent_category_id IN NUMBER,
    p_delete_flag       IN NUMBER
)
AS
BEGIN
    IF p_delete_flag = 1 THEN
        UPDATE category_central
        SET delete_flag = 1
        WHERE category_id = p_category_id;
    ELSE
        MERGE INTO category_central t
        USING (SELECT p_category_id AS category_id FROM dual) s
        ON (t.category_id = s.category_id)
        WHEN MATCHED THEN
            UPDATE SET
                t.user_id            = p_user_id,
                t.name               = p_name,
                t.type               = p_type,
                t.parent_category_id = NVL(p_parent_category_id, t.parent_category_id),
                t.delete_flag        = 0
        WHEN NOT MATCHED THEN
            INSERT (category_id, user_id, name, type, parent_category_id, delete_flag)
            VALUES (p_category_id, p_user_id, p_name, p_type, p_parent_category_id, 0);
    END IF;
    COMMIT;
END proc_sync_category;
/

// Expense Sync Procedure
CREATE OR REPLACE PROCEDURE proc_sync_expense (
    p_expense_id     IN NUMBER,
    p_device_txn_id  IN VARCHAR2,
    p_user_id        IN NUMBER,
    p_account_id     IN NUMBER,
    p_category_id    IN NUMBER,
    p_amount         IN NUMBER,
    p_currency       IN VARCHAR2,
    p_expense_date   IN DATE,
    p_description    IN VARCHAR2,
    p_recurring_flag IN NUMBER,
    p_sync_status    IN VARCHAR2,
    p_created_at     IN TIMESTAMP,
    p_modified_at    IN TIMESTAMP,
    p_delete_flag    IN NUMBER
)
AS
BEGIN
    IF p_delete_flag = 1 THEN
        UPDATE expense_central
        SET delete_flag = 1
        WHERE expense_id = p_expense_id;
    ELSE
        MERGE INTO expense_central t
        USING (SELECT p_expense_id AS expense_id FROM dual) s
        ON (t.expense_id = s.expense_id)
        WHEN MATCHED THEN
            UPDATE SET
                t.device_txn_id  = p_device_txn_id,
                t.user_id        = p_user_id,
                t.account_id     = p_account_id,
                t.category_id    = p_category_id,
                t.amount         = p_amount,
                t.currency       = p_currency,
                t.expense_date   = NVL(p_expense_date, t.expense_date),
                t.description    = p_description,
                t.recurring_flag = p_recurring_flag,
                t.sync_status    = NVL(p_sync_status, t.sync_status),
                t.modified_at    = NVL(p_modified_at, t.modified_at),
                t.delete_flag    = 0
        WHEN NOT MATCHED THEN
            INSERT (expense_id, device_txn_id, user_id, account_id, category_id, amount, currency,
                    expense_date, description, recurring_flag, sync_status, created_at, modified_at, delete_flag)
            VALUES (p_expense_id, p_device_txn_id, p_user_id, p_account_id, p_category_id, p_amount, p_currency,
                    p_expense_date, p_description, p_recurring_flag, p_sync_status, p_created_at, p_modified_at, 0);
    END IF;
    COMMIT;
END proc_sync_expense;
/

// Savings Goal Sync Procedure
CREATE OR REPLACE PROCEDURE proc_sync_savings_goal (
    p_goal_id      IN NUMBER,
    p_user_id      IN NUMBER,
    p_name         IN VARCHAR2,
    p_target_amount IN NUMBER,
    p_current_amount IN NUMBER,
    p_start_date   IN DATE,
    p_target_date  IN DATE,
    p_status       IN VARCHAR2,
    p_delete_flag  IN NUMBER
)
AS
BEGIN
    IF p_delete_flag = 1 THEN
        UPDATE savings_goal_central
        SET delete_flag = 1
        WHERE goal_id = p_goal_id;
    ELSE
        MERGE INTO savings_goal_central t
        USING (SELECT p_goal_id AS goal_id FROM dual) s
        ON (t.goal_id = s.goal_id)
        WHEN MATCHED THEN
            UPDATE SET
                t.user_id        = p_user_id,
                t.name           = p_name,
                t.target_amount  = p_target_amount,
                t.current_amount = p_current_amount,
                t.start_date     = NVL(p_start_date, t.start_date),
                t.target_date    = NVL(p_target_date, t.target_date),
                t.status         = NVL(p_status, t.status),
                t.delete_flag    = 0
        WHEN NOT MATCHED THEN
            INSERT (goal_id, user_id, name, target_amount, current_amount, start_date, target_date, status, delete_flag)
            VALUES (p_goal_id, p_user_id, p_name, p_target_amount, p_current_amount, p_start_date, p_target_date, p_status, 0);
    END IF;
    COMMIT;
END proc_sync_savings_goal;
/

// Sync Log Sync Procedure
CREATE OR REPLACE PROCEDURE proc_sync_sync_log (
    p_sync_id       IN NUMBER,
    p_device_txn_id IN VARCHAR2,
    p_table_name    IN VARCHAR2,
    p_status        IN VARCHAR2,
    p_last_attempt  IN TIMESTAMP,
    p_retries       IN NUMBER
)
AS
BEGIN
    MERGE INTO sync_log_central t
    USING (SELECT p_sync_id AS sync_id FROM dual) s
    ON (t.sync_id = s.sync_id)
    WHEN MATCHED THEN
        UPDATE SET
            t.device_txn_id = p_device_txn_id,
            t.table_name    = p_table_name,
            t.status        = p_status,
            t.last_attempt  = NVL(p_last_attempt, t.last_attempt),
            t.retries       = NVL(p_retries, t.retries)
    WHEN NOT MATCHED THEN
        INSERT (sync_id, device_txn_id, table_name, status, last_attempt, retries)
        VALUES (p_sync_id, p_device_txn_id, p_table_name, p_status, p_last_attempt, p_retries);

    COMMIT;
END proc_sync_sync_log;
/

