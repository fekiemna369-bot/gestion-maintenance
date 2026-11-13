package maintenance.utilisateur.dao;
import maintenance.utilisateur.model.User;
import java.sql.*;
import java.util.*;
import maintenance.config.DBConnection;
public class UserDAO {
    public record AuthRecord(User user, String passwordHash) {}

    public Optional<AuthRecord> findForLogin(String email) throws SQLException {
        String sql = "SELECT utilisateur_id, nom, prenom, email, mot_de_passe_hash, role, actif " +
                     "FROM UTILISATEURS WHERE email = ? AND actif = TRUE";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                User user = new User(rs.getLong("utilisateur_id"), rs.getString("nom"),
                        rs.getString("prenom"), rs.getString("email"), rs.getString("role"), rs.getBoolean("actif"));
                return Optional.of(new AuthRecord(user, rs.getString("mot_de_passe_hash")));
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT utilisateur_id, nom, prenom, email, role, actif FROM UTILISATEURS ORDER BY nom, prenom";
        List<User> users = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(new User(rs.getLong("utilisateur_id"), rs.getString("nom"),
                    rs.getString("prenom"), rs.getString("email"), rs.getString("role"), rs.getBoolean("actif")));
        }
        return users;
    }

    public void create(String nom, String prenom, String email, String passwordHash, String role) throws SQLException {
        if (!List.of("requester", "technician", "maintenance_manager").contains(role)) {
            throw new IllegalArgumentException("Rôle invalide");
        }
        String sql = "INSERT INTO UTILISATEURS (nom, prenom, email, mot_de_passe_hash, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom.trim());
            ps.setString(2, prenom.trim());
            ps.setString(3, email.trim().toLowerCase());
            ps.setString(4, passwordHash);
            ps.setString(5, role);
            ps.executeUpdate();
        }
    }
}
