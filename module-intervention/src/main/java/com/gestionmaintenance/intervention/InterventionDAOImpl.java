package com.gestionmaintenance.intervention;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** MySQL implementation of InterventionDAO. */
public class InterventionDAOImpl implements InterventionDAO {

    @Override
    public int inserer(Connection connection, int affectationId) throws SQLException {
        String sql = "INSERT INTO INTERVENTIONS (affectation_id) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, affectationId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("La base n'a pas retourné l'identifiant de l'intervention.");
    }

    @Override
    public Integer trouverIdParAffectation(Connection connection, int affectationId) throws SQLException {
        String sql = "SELECT intervention_id FROM INTERVENTIONS WHERE affectation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, affectationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("intervention_id") : null;
            }
        }
    }

    @Override
    public Intervention trouverParAffectation(Connection connection, int affectationId)
            throws SQLException {
        String sql = "SELECT intervention_id, affectation_id, date_debut_reelle, date_fin_reelle, " +
                "duree_reelle_heures, cause_panne, action_realisee, date_creation " +
                "FROM INTERVENTIONS WHERE affectation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, affectationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Intervention intervention = new Intervention();
                intervention.setInterventionId(rs.getInt("intervention_id"));
                intervention.setAffectationId(rs.getInt("affectation_id"));
                Timestamp debut = rs.getTimestamp("date_debut_reelle");
                Timestamp fin = rs.getTimestamp("date_fin_reelle");
                Timestamp creation = rs.getTimestamp("date_creation");
                if (debut != null) intervention.setDateDebutReelle(debut.toLocalDateTime());
                if (fin != null) intervention.setDateFinReelle(fin.toLocalDateTime());
                if (creation != null) intervention.setDateCreation(creation.toLocalDateTime());
                intervention.setDureeReelleHeures(rs.getBigDecimal("duree_reelle_heures"));
                intervention.setCausePanne(rs.getString("cause_panne"));
                intervention.setActionRealisee(rs.getString("action_realisee"));
                return intervention;
            }
        }
    }

    @Override
    public void enregistrerDebut(Connection connection, int interventionId, LocalDateTime dateDebut)
            throws SQLException {
        String sql = "UPDATE INTERVENTIONS SET date_debut_reelle = ? " +
                "WHERE intervention_id = ? AND date_debut_reelle IS NULL AND date_fin_reelle IS NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dateDebut));
            ps.setInt(2, interventionId);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("L'intervention a déjà été démarrée ou terminée.");
            }
        }
    }

    @Override
    public void enregistrerFin(Connection connection, int interventionId, LocalDateTime dateFin,
                               BigDecimal dureeReelleHeures, String causePanne,
                               String actionRealisee) throws SQLException {
        String sql = "UPDATE INTERVENTIONS SET date_fin_reelle = ?, duree_reelle_heures = ?, " +
                "cause_panne = ?, action_realisee = ? " +
                "WHERE intervention_id = ? AND date_debut_reelle IS NOT NULL AND date_fin_reelle IS NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dateFin));
            ps.setBigDecimal(2, dureeReelleHeures);
            ps.setString(3, causePanne);
            ps.setString(4, actionRealisee);
            ps.setInt(5, interventionId);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("L'intervention est déjà terminée ou n'a pas commencé.");
            }
        }
    }
}
