package com.example.db;

public class DBConfig {

    // SQLite Local Database
    public static final String SQLITE_URL = "jdbc:sqlite:database/personalfinanceDB.db";
    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    // Oracle Central Database
    public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    public static final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    public static final String ORACLE_USER = "system";
    public static final String ORACLE_PASSWORD = "sys123";

    // Common settings
    public static final int CONNECTION_TIMEOUT = 10;
}
