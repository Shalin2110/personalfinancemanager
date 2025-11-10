-- Connect as SYSTEM first
CREATE USER C##finance_app IDENTIFIED BY SecurePass123;
GRANT CONNECT, RESOURCE TO C##finance_app;

-- Grant specific privileges you might need
GRANT CREATE SESSION TO C##finance_app;
GRANT UNLIMITED TABLESPACE TO C##finance_app;