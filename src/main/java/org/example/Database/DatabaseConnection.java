package org.example.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:postgresql://dpg-d9acd9t7vvec738uksr0-a.oregon-postgres.render.com:5432/test_hzrz?sslmode=require";

    private static final String USER = "test_hzrz_user";
    private static final String PASSWORD = "5R0iID32VNHmRaQnP2nkTPiU9yW5ZBut";

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver"); // Optional for newer JDBC versions
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to connect to PostgreSQL", e);
        }
    }
}