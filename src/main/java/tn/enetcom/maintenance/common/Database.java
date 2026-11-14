package tn.enetcom.maintenance.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String URL  = "jdbc:mysql://localhost:3306/gestion_maintenance?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "1234";

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new ExceptionInInitializerError(e); }
    }

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}