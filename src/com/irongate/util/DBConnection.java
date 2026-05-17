package com.irongate.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://Muhammad-Irtaza\\SQLEXPRESS;" +
            "databaseName=IronGate Vault;" +
            "integratedSecurity=true;" +
            "trustServerCertificate=true;";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQL Server JDBC driver not found. Add mssql-jdbc jar to lib/", e);
            }
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
