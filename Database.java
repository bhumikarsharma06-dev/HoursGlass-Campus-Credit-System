package com.hourglass.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() { }

    public static Connection open() throws SQLException {
        String url = System.getenv().getOrDefault("HOURGLASS_DB_URL", "jdbc:mysql://localhost:3306/hourglass?serverTimezone=UTC");
        String user = System.getenv().getOrDefault("HOURGLASS_DB_USER", "root");
        String password = System.getenv().getOrDefault("HOURGLASS_DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }
}
