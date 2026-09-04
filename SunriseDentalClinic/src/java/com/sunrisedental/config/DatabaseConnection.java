package com.sunrisedental.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton Pattern — single shared JDBC connection factory for the application.
 */
public final class DatabaseConnection {

    private static DatabaseConnection instance;
    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnection() {
        Properties props = loadProperties();
        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.username", "root");
        this.password = props.getProperty("db.password", "");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found. Add mysql-connector-j.jar to WEB-INF/lib", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found in WEB-INF/classes");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load db.properties", e);
        }
        return props;
    }
}
