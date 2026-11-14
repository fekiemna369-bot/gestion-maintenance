package tn.enetcom.maintenance.equipement.dao;

import tn.enetcom.maintenance.common.Database;
import tn.enetcom.maintenance.equipement.model.Equipement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipementDAOImpl implements EquipementDAO {

    @Override
    public Equipement findById(int id) throws SQLException {
        String sql = "SELECT equipement_id, designation, lieu FROM EQUIPEMENTS WHERE equipement_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    @Override
    public List<Equipement> findAll() throws SQLException {
        String sql = "SELECT equipement_id, designation, lieu FROM EQUIPEMENTS ORDER BY designation";
        List<Equipement> liste = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    @Override
    public List<Equipement> findByLieu(String lieu) throws SQLException {
        String sql = "SELECT equipement_id, designation, lieu FROM EQUIPEMENTS WHERE lieu = ? ORDER BY designation";
        List<Equipement> liste = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, lieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    @Override
    public int insert(Equipement e) throws SQLException {
        String sql = "INSERT INTO EQUIPEMENTS (designation, lieu) VALUES (?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getDesignation());
            ps.setString(2, e.getLieu());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Échec insertion équipement, aucun id généré.");
    }

    @Override
    public boolean update(Equipement e) throws SQLException {
        String sql = "UPDATE EQUIPEMENTS SET designation = ?, lieu = ? WHERE equipement_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getDesignation());
            ps.setString(2, e.getLieu());
            ps.setInt(3, e.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM EQUIPEMENTS WHERE equipement_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean existeParDesignationEtLieu(String designation, String lieu) throws SQLException {
        String sql = "SELECT 1 FROM EQUIPEMENTS WHERE designation = ? AND lieu = ? LIMIT 1";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, designation);
            ps.setString(2, lieu);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int compterDemandesLiees(int idEquipement) throws SQLException {
        String sql = "SELECT COUNT(*) FROM DEMANDES WHERE equipement_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEquipement);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("doesn't exist")) return 0;
            throw ex;
        }
    }

    private Equipement mapper(ResultSet rs) throws SQLException {
        return new Equipement(
                rs.getInt("equipement_id"),
                rs.getString("designation"),
                rs.getString("lieu")
        );
    }
}