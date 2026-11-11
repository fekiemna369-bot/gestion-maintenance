package com.maintenance.technician.repository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fournit une connexion MySQL. À brancher sur la classe de connexion commune de l'équipe
 * (DriverManager, DataSource Spring, pool Jakarta...). Exemple :
 *
 *   ConnectionProvider cp = () -> DriverManager.getConnection(
 *       "jdbc:mysql://localhost:3306/gestion_maintenance", "root", "motdepasse");
 */
@FunctionalInterface
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
