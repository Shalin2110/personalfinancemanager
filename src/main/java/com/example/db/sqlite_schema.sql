--User Table
CREATE TABLE user (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    email TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    delete_flag INTEGER DEFAULT 0
);
--Account Table
CREATE TABLE account (
    account_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    currency TEXT DEFAULT 'LKR',
    opening_balance REAL DEFAULT 0,
    delete_flag INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);
--Category Table
CREATE TABLE category (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    type TEXT CHECK (type IN ('EXPENSE','INCOME')),
    parent_category_id INTEGER,
    delete_flag INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (parent_category_id) REFERENCES category(category_id)
);
--Expense Table
CREATE TABLE expense (
    expense_id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_txn_id TEXT UNIQUE,
    user_id INTEGER NOT NULL,
    account_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    currency TEXT DEFAULT 'LKR',
    date DATE NOT NULL,
    description TEXT,
    recurring_flag INTEGER DEFAULT 0,
    sync_status TEXT DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME,
    delete_flag INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);
--Budget Table
CREATE TABLE budget (
    budget_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    amount REAL NOT NULL,
    alert_threshold_pct REAL DEFAULT 80,
    delete_flag INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);
--Savings Goal Table
CREATE TABLE savings_goal (
    goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    target_amount REAL NOT NULL,
    current_amount REAL DEFAULT 0,
    start_date DATE,
    target_date DATE,
    status TEXT DEFAULT 'ACTIVE',
    delete_flag INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);
--Sync Log Table
CREATE TABLE sync_log (
    sync_id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_txn_id TEXT NOT NULL,
    table_name TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    last_attempt DATETIME DEFAULT CURRENT_TIMESTAMP,
    retries INTEGER DEFAULT 0
);


