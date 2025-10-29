--User Table
CREATE TABLE user_central (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(100) UNIQUE NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    email VARCHAR2(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_flag NUMBER(1) DEFAULT 0
);
--Account Table
CREATE TABLE account_central (
    account_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL,
    name VARCHAR2(100) NOT NULL,
    currency VARCHAR2(10) DEFAULT 'LKR',
    opening_balance NUMBER(12,2) DEFAULT 0,
    delete_flag NUMBER(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_central(user_id)
);
--Category Table
CREATE TABLE category_central (
    category_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL,
    name VARCHAR2(100) NOT NULL,
    type VARCHAR2(10) CHECK (type IN ('EXPENSE','INCOME')),
    parent_category_id NUMBER,
    delete_flag NUMBER(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_central(user_id),
    FOREIGN KEY (parent_category_id) REFERENCES category_central(category_id)
);
--Expense Table
CREATE TABLE expense_central (
    expense_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    device_txn_id VARCHAR2(50) UNIQUE,
    user_id NUMBER NOT NULL,
    account_id NUMBER NOT NULL,
    category_id NUMBER NOT NULL,
    amount NUMBER(12,2) NOT NULL,
    currency VARCHAR2(10) DEFAULT 'LKR',
    expense_date DATE NOT NULL,
    description VARCHAR2(255),
    recurring_flag NUMBER(1) DEFAULT 0,
    sync_status VARCHAR2(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP,
    delete_flag NUMBER(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_central(user_id),
    FOREIGN KEY (account_id) REFERENCES account_central(account_id),
    FOREIGN KEY (category_id) REFERENCES category_central(category_id)
);
--Budget Table
CREATE TABLE budget_central (
    budget_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL,
    category_id NUMBER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    amount NUMBER(12,2) NOT NULL,
    alert_threshold_pct NUMBER(5,2) DEFAULT 80,
    delete_flag NUMBER(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_central(user_id),
    FOREIGN KEY (category_id) REFERENCES category_central(category_id)
);
--Savings Goal Table
CREATE TABLE savings_goal_central (
    goal_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL,
    name VARCHAR2(100) NOT NULL,
    target_amount NUMBER(12,2) NOT NULL,
    current_amount NUMBER(12,2) DEFAULT 0,
    start_date DATE,
    target_date DATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE',
    delete_flag NUMBER(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_central(user_id)
);
--Sync Log Table
CREATE TABLE sync_log_central (
    sync_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    device_txn_id VARCHAR2(50) NOT NULL,
    table_name VARCHAR2(50) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING',
    last_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    retries NUMBER DEFAULT 0
);
