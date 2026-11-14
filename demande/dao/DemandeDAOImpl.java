package tn.enetcom.maintenance.demande.dao;

import tn.enetcom.maintenance.common.Database;
import tn.enetcom.maintenance.demande.model.Demande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeDAOImpl implements DemandeDAO {

    private static final String COLS =
        "demande_id, demandeur_id, equipement_id, competence_id, description, " +
        "priorite, duree_estimee_heures, statut, date_signalement";

    @Override
    public Demande findById(int id) throws SQLException {
        String sql = "SELECT " + COLS + " FROM DEMANDES WHERE demande_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    @Override
    public List<Demande> findAll() throws SQLException {
        String sql = "SELECT " + COLS + " FROM DEMANDES ORDER BY date_signalement DESC";
        List<Demande> liste = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    @Override
    public List<Demande> findByDemandeur(int demandeurId) throws SQLException {
        String sql = "SELECT " + COLS + " FROM DEMANDES WHERE demandeur_id = ? ORDER BY date_signalement DESC";
        List<Demande> liste = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, demandeurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    @Override
    public List<Demande> findByStatut(Demande.Statut statut) throws SQLException {
        String sql = "SELECT " + COLS + " FROM DEMANDES WHERE statut = ? ORDER BY date_signalement DESC";
        List<Demande> liste = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut.name().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    @Override
    public int insert(Demande d) throws SQLException {
        String sql = "INSERT INTO DEMANDES " +
            "(demandeur_id, equipement_id, competence_id, description, priorite, " +
            "duree_estimee_heures, statut, date_signalement) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getDemandeurId());
            ps.setInt(2, d.getEquipementId());
            ps.setInt(3, d.getCompetenceId());
            ps.setString(4, d.getDescription());
            ps.setString(5, d.getPriorite().name().toLowerCase());
            ps.setDouble(6, d.getDureeEstimeeHeures());
            ps.setString(7, d.getStatut().name().toLowerCase());
            ps.setTimestamp(8, Timestamp.valueOf(d.getDateSignalement()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Échec insertion demande, aucun id généré.");
    }

    @Override
    public boolean updateStatut(int id, Demande.Statut statut) throws SQLException {
        String sql = "UPDATE DEMANDES SET statut = ? WHERE demande_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut.name().toLowerCase());
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean update(Demande d) throws SQLException {
        String sql = "UPDATE DEMANDES SET equipement_id=?, competence_id=?, description=?, " +
            "priorite=?, duree_estimee_heures=?, statut=?, date_signalement=? WHERE demande_id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, d.getEquipementId());
            ps.setInt(2, d.getCompetenceId());
            ps.setString(3, d.getDescription());
            ps.setString(4, d.getPriorite().name().toLowerCase());
            ps.setDouble(5, d.getDureeEstimeeHeures());
            ps.setString(6, d.getStatut().name().toLowerCase());
            ps.setTimestamp(7, Timestamp.valueOf(d.getDateSignalement()));
            ps.setInt(8, d.getId());
            return ps.executeUpdate() == 1;
        }
    }

    private Demande mapper(ResultSet rs) throws SQLException {
        Demande d = new Demande();
        d.setId(rs.getInt("demande_id"));
        d.setDemandeurId(rs.getInt("demandeur_id"));
        d.setEquipementId(rs.getInt("equipement_id"));
        d.setCompetenceId(rs.getInt("competence_id"));
        d.setDescription(rs.getString("description"));
        d.setPriorite(Demande.Priorite.valueOf(rs.getString("priorite").toUpperCase()));
        d.setDureeEstimeeHeures(rs.getDouble("duree_estimee_heures"));
        d.setStatut(Demande.Statut.valueOf(rs.getString("statut").toUpperCase()));
        d.setDateSignalement(rs.getTimestamp("date_signalement").toLocalDateTime());
        return d;
    }
}