package com.maintenance.technician.repository;

import com.maintenance.technician.model.Skill;
import com.maintenance.technician.model.Technician;
import com.maintenance.technician.model.Unavailability;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;


public class TechnicianRepository {

    private static final String SELECT_TECHNICIAN =
            "SELECT t.technicien_id, t.utilisateur_id, t.capacite_journaliere_heures, "
          + "       u.prenom, u.nom, u.email "
          + "FROM TECHNICIENS t JOIN UTILISATEURS u ON u.utilisateur_id = t.utilisateur_id ";

    private final ConnectionProvider db;

    public TechnicianRepository(ConnectionProvider db) {
        this.db = db;
    }

    public int create(int userId, BigDecimal dailyCapacity) throws SQLException {
        String sql = "INSERT INTO TECHNICIENS (utilisateur_id, capacite_journaliere_heures) VALUES (?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, dailyCapacity);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void updateCapacity(int technicianId, BigDecimal dailyCapacity) throws SQLException {
        String sql = "UPDATE TECHNICIENS SET capacite_journaliere_heures = ? WHERE technicien_id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, dailyCapacity);
            ps.setInt(2, technicianId);
            ps.executeUpdate();
        }
    }


    public void delete(int technicianId) throws SQLException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM TECHNICIENS WHERE technicien_id = ?")) {
            ps.setInt(1, technicianId);
            ps.executeUpdate();
        }
    }

    public boolean hasAssignments(int technicianId) throws SQLException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM AFFECTATIONS WHERE technicien_id = ? LIMIT 1")) {
            ps.setInt(1, technicianId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Optional<Technician> findById(int technicianId) throws SQLException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_TECHNICIAN + "WHERE t.technicien_id = ?")) {
            ps.setInt(1, technicianId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Technician t = map(rs);
                t.setSkills(findSkills(c, technicianId));
                return Optional.of(t);
            }
        }
    }

    public Optional<Technician> findByUserId(int userId) throws SQLException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_TECHNICIAN + "WHERE t.utilisateur_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Technician t = map(rs);
                t.setSkills(findSkills(c, t.getId()));
                return Optional.of(t);
            }
        }
    }

    /** Tous les techniciens actifs, avec leurs compétences, triés par prénom. */
    public List<Technician> findAll() throws SQLException {
        String sql = SELECT_TECHNICIAN + "WHERE u.actif = TRUE ORDER BY u.prenom, u.nom";
        List<Technician> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            for (Technician t : list) t.setSkills(findSkills(c, t.getId()));
        }
        return list;
    }


    public List<Skill> findAllSkills() throws SQLException {
        List<Skill> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT competence_id, nom FROM COMPETENCES ORDER BY nom");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Skill(rs.getInt(1), rs.getString(2)));
        }
        return list;
    }

    public void addSkill(int technicianId, int skillId) throws SQLException {
        String sql = "INSERT IGNORE INTO TECHNICIEN_COMPETENCES (technicien_id, competence_id) VALUES (?, ?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            ps.setInt(2, skillId);
            ps.executeUpdate();
        }
    }

    public void removeSkill(int technicianId, int skillId) throws SQLException {
        String sql = "DELETE FROM TECHNICIEN_COMPETENCES WHERE technicien_id = ? AND competence_id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            ps.setInt(2, skillId);
            ps.executeUpdate();
        }
    }

    /** Remplace toutes les compétences d'un technicien (pratique pour un formulaire à cases à cocher). */
    public void replaceSkills(int technicianId, Collection<Integer> skillIds) throws SQLException {
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement(
                         "DELETE FROM TECHNICIEN_COMPETENCES WHERE technicien_id = ?");
                 PreparedStatement ins = c.prepareStatement(
                         "INSERT INTO TECHNICIEN_COMPETENCES (technicien_id, competence_id) VALUES (?, ?)")) {
                del.setInt(1, technicianId);
                del.executeUpdate();
                for (int skillId : new LinkedHashSet<>(skillIds)) {
                    ins.setInt(1, technicianId);
                    ins.setInt(2, skillId);
                    ins.addBatch();
                }
                ins.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }


    /** Insère un jour d'indisponibilité ; ignoré s'il existe déjà (contrainte UNIQUE technicien+date). */
    public void addUnavailability(int technicianId, LocalDate date, String reason) throws SQLException {
        String sql = "INSERT IGNORE INTO INDISPONIBILITES (technicien_id, date_indisponible, motif) VALUES (?, ?, ?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setString(3, reason);
            ps.executeUpdate();
        }
    }

    public void deleteUnavailability(int unavailabilityId) throws SQLException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM INDISPONIBILITES WHERE indisponibilite_id = ?")) {
            ps.setInt(1, unavailabilityId);
            ps.executeUpdate();
        }
    }

    public boolean isUnavailable(int technicianId, LocalDate date) throws SQLException {
        String sql = "SELECT 1 FROM INDISPONIBILITES WHERE technicien_id = ? AND date_indisponible = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Indisponibilités d'un technicien entre deux dates incluses (null = sans borne). */
    public List<Unavailability> findUnavailabilities(int technicianId, LocalDate from, LocalDate to)
            throws SQLException {
        String sql = "SELECT indisponibilite_id, technicien_id, date_indisponible, motif "
                   + "FROM INDISPONIBILITES WHERE technicien_id = ? "
                   + "AND (? IS NULL OR date_indisponible >= ?) AND (? IS NULL OR date_indisponible <= ?) "
                   + "ORDER BY date_indisponible";
        List<Unavailability> list = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            java.sql.Date f = from == null ? null : java.sql.Date.valueOf(from);
            java.sql.Date t = to == null ? null : java.sql.Date.valueOf(to);
            ps.setInt(1, technicianId);
            ps.setDate(2, f);
            ps.setDate(3, f);
            ps.setDate(4, t);
            ps.setDate(5, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Unavailability(rs.getInt(1), rs.getInt(2),
                            rs.getDate(3).toLocalDate(), rs.getString(4)));
                }
            }
        }
        return list;
    }

    // ------------------------------------------------------------------ charge

    /**
     * Charge d'un technicien un jour donné = somme des durées estimées de ses affectations
     * validées ce jour-là, sans compter les demandes annulées.
     * Une intervention terminée compte toujours (le temps a bien été réservé ce jour-là).
     */
    public BigDecimal sumDailyLoad(int technicianId, LocalDate date) throws SQLException {
        String sql = "SELECT COALESCE(SUM(a.duree_estimee_heures), 0) "
                   + "FROM AFFECTATIONS a JOIN DEMANDES d ON d.demande_id = a.demande_id "
                   + "WHERE a.technicien_id = ? AND a.date_intervention_prevue = ? "
                   + "  AND a.statut = 'validated' "
                   + "  AND d.statut IN ('assigned', 'in_progress', 'completed')";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    private Technician map(ResultSet rs) throws SQLException {
        Technician t = new Technician(
                rs.getInt("technicien_id"),
                rs.getInt("utilisateur_id"),
                rs.getString("prenom"),
                rs.getString("nom"),
                rs.getBigDecimal("capacite_journaliere_heures"));
        t.setEmail(rs.getString("email"));
        return t;
    }

    private List<Skill> findSkills(Connection c, int technicianId) throws SQLException {
        String sql = "SELECT co.competence_id, co.nom FROM TECHNICIEN_COMPETENCES tc "
                   + "JOIN COMPETENCES co ON co.competence_id = tc.competence_id "
                   + "WHERE tc.technicien_id = ? ORDER BY co.nom";
        List<Skill> skills = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) skills.add(new Skill(rs.getInt(1), rs.getString(2)));
            }
        }
        return skills;
    }
}
