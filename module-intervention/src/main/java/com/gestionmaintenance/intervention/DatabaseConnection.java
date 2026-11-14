package com.gestionmaintenance.intervention;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens a MySQL connection using environment variables (no credentials in source). */
public final class DatabaseConnection {
    private DatabaseConnection() { }

    public static Connection ouvrir() throws SQLException {
        String url = required("DB_URL");
        String user = required("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = "";
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("La variable d'environnement " + name + " est obligatoire.");
        }
        return value;
    }
}
