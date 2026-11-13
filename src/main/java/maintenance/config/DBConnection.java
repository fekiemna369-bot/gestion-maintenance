package maintenance.config;
	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.SQLException;

	public final class DBConnection {
	    private DBConnection() {}

	    public static Connection getConnection() throws SQLException {
	        String url = requiredEnv("DB_URL",
	                "jdbc:mysql://localhost:3306/gestion_maintenance?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8");
	        String user = requiredEnv("DB_USER", "root");
	        String password = requiredEnv("DB_PASSWORD", "");
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	        } catch (ClassNotFoundException e) {
	            throw new SQLException("MySQL JDBC driver not found", e);
	        }
	        return DriverManager.getConnection(url, user, password);
	    }

	    private static String requiredEnv(String name, String defaultValue) {
	        String value = System.getenv(name);
	        return value == null ? defaultValue : value;
	    }
	}



