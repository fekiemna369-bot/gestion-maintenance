package com.gestionmaintenance.intervention;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Business operations for manual assignment and the intervention lifecycle. */
public class AffectationInterventionService {
    private final InterventionDAO interventionDAO;

    public AffectationInterventionService() {
        this(new InterventionDAOImpl());
    }

    public AffectationInterventionService(InterventionDAO interventionDAO) {
        if (interventionDAO == null) throw new IllegalArgumentException("Le DAO est obligatoire.");
        this.interventionDAO = interventionDAO;
    }

    /** Assigns an open request, creates a validated schedule and its intervention row. */
    public int affecterDemande(int responsableId, int demandeId, int technicienId,
                               LocalDate dateIntervention) throws SQLException {
        if (dateIntervention == null) {
            throw new IllegalArgumentException("La date d'intervention est obligatoire.");
        }

        try (Connection c = DatabaseConnection.ouvrir()) {
            c.setAutoCommit(false);
            try {
                verifierRole(c, responsableId, "maintenance_manager");

                // Lock the technician row so two concurrent assignments cannot overbook the same day.
                verifierTechnicienEtVerrouiller(c, technicienId);
                Demande demande = lireDemandePourMiseAJour(c, demandeId);
                if (!"open".equals(demande.statut)) {
                    throw new IllegalStateException("La demande doit être ouverte pour être affectée.");
                }
                if (demande.dateSignalement.toLocalDateTime().toLocalDate().isAfter(dateIntervention)) {
                    throw new IllegalArgumentException("La demande n'a pas encore été signalée à la date planifiée.");
                }

                verifierCompetence(c, demande.competenceId, technicienId);
                verifierDisponibilite(c, technicienId, dateIntervention);
                verifierCapacite(c, technicienId, dateIntervention, demande.dureeEstimee);

                int planificationId = creerPlanification(c, responsableId, dateIntervention);
                BigDecimal score = calculerScore(demande, dateIntervention);
                int affectationId = creerAffectation(c, planificationId, demande, technicienId,
                        dateIntervention, score);
                enregistrerScorePlanification(c, planificationId, score);
                marquerDemandeAssignee(c, demandeId);
                interventionDAO.inserer(c, affectationId);
                c.commit();
                return affectationId;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Starts the intervention; only its assigned technician may do so. */
    public void demarrer(int utilisateurTechnicienId, int interventionId) throws SQLException {
        executerTransitionTechnicien(utilisateurTechnicienId, interventionId, false,
                null, null, null);
    }

    /** Completes an intervention when a full report and positive actual duration are provided. */
    public void terminer(int utilisateurTechnicienId, int interventionId,
                         String causePanne, String actionRealisee,
                         BigDecimal dureeReelleHeures) throws SQLException {
        if (vide(causePanne) || vide(actionRealisee)) {
            throw new IllegalArgumentException("La cause de panne et l'action réalisée sont obligatoires.");
        }
        if (dureeReelleHeures == null || dureeReelleHeures.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La durée réelle doit être supérieure à zéro.");
        }
        executerTransitionTechnicien(utilisateurTechnicienId, interventionId, true,
                causePanne.trim(), actionRealisee.trim(), dureeReelleHeures);
    }

    /** Returns the intervention ID associated with an assignment ID. */
    public int trouverInterventionIdParAffectation(int affectationId) throws SQLException {
        try (Connection c = DatabaseConnection.ouvrir()) {
            Integer interventionId = interventionDAO.trouverIdParAffectation(c, affectationId);
            if (interventionId == null) {
                throw new IllegalArgumentException("Aucune intervention ne correspond à cette affectation.");
            }
            return interventionId;
        }
    }

    private void executerTransitionTechnicien(int utilisateurId, int interventionId, boolean terminer,
                                               String cause, String action, BigDecimal duree)
            throws SQLException {
        try (Connection c = DatabaseConnection.ouvrir()) {
            c.setAutoCommit(false);
            try {
                verifierRole(c, utilisateurId, "technician");
                InterventionContexte contexte = lireContexteIntervention(c, interventionId);
                if (contexte.utilisateurTechnicienId != utilisateurId) {
                    throw new IllegalStateException("Cette intervention n'est pas affectée à ce technicien.");
                }

                if (!terminer) {
                    if (!"assigned".equals(contexte.statutDemande) || contexte.dateDebut != null) {
                        throw new IllegalStateException("Seule une intervention assignée peut être démarrée.");
                    }
                    interventionDAO.enregistrerDebut(c, interventionId, LocalDateTime.now());
                    changerStatutDemande(c, contexte.demandeId, "assigned", "in_progress");
                } else {
                    if (!"in_progress".equals(contexte.statutDemande) || contexte.dateDebut == null) {
                        throw new IllegalStateException("L'intervention doit être en cours avant de la terminer.");
                    }
                    LocalDateTime fin = LocalDateTime.now();
                    if (contexte.dateDebut.toLocalDateTime().isAfter(fin)) {
                        throw new IllegalStateException("La date de début est postérieure à la date actuelle.");
                    }
                    interventionDAO.enregistrerFin(c, interventionId, fin, duree, cause, action);
                    changerStatutDemande(c, contexte.demandeId, "in_progress", "completed");
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void verifierRole(Connection c, int utilisateurId, String role) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT role, actif FROM UTILISATEURS WHERE utilisateur_id = ?")) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !role.equals(rs.getString("role")) || !rs.getBoolean("actif")) {
                    throw new IllegalStateException("Utilisateur introuvable, inactif ou rôle non autorisé.");
                }
            }
        }
    }

    private void verifierTechnicienEtVerrouiller(Connection c, int technicienId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT t.technicien_id FROM TECHNICIENS t " +
                "JOIN UTILISATEURS u ON u.utilisateur_id = t.utilisateur_id " +
                "WHERE t.technicien_id = ? AND u.actif = TRUE AND u.role = 'technician' FOR UPDATE")) {
            ps.setInt(1, technicienId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Technicien introuvable ou inactif.");
                }
            }
        }
    }

    private Demande lireDemandePourMiseAJour(Connection c, int demandeId) throws SQLException {
        String sql = "SELECT demande_id, competence_id, duree_estimee_heures, statut, date_signalement, priorite " +
                     "FROM DEMANDES WHERE demande_id = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, demandeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Demande introuvable.");
                }
                Demande d = new Demande();
                d.id = rs.getInt("demande_id");
                d.competenceId = rs.getInt("competence_id");
                d.dureeEstimee = rs.getBigDecimal("duree_estimee_heures");
                d.statut = rs.getString("statut");
                d.dateSignalement = rs.getTimestamp("date_signalement");
                d.priorite = rs.getString("priorite");
                return d;
            }
        }
    }

    private void verifierCompetence(Connection c, int competenceId, int technicienId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM TECHNICIEN_COMPETENCES WHERE competence_id = ? AND technicien_id = ?")) {
            ps.setInt(1, competenceId);
            ps.setInt(2, technicienId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Conflit : le technicien ne possède pas la compétence requise.");
                }
            }
        }
    }

    private void verifierDisponibilite(Connection c, int technicienId, LocalDate date) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM INDISPONIBILITES WHERE technicien_id = ? AND date_indisponible = ?")) {
            ps.setInt(1, technicienId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalStateException("Conflit : le technicien est indisponible à cette date.");
                }
            }
        }
    }

    private void verifierCapacite(Connection c, int technicienId, LocalDate date,
                                  BigDecimal nouvelleDuree) throws SQLException {
        String sql = "SELECT t.capacite_journaliere_heures, " +
                "COALESCE(SUM(CASE WHEN a.statut = 'validated' THEN a.duree_estimee_heures ELSE 0 END), 0) AS charge " +
                "FROM TECHNICIENS t LEFT JOIN AFFECTATIONS a " +
                "ON a.technicien_id = t.technicien_id AND a.date_intervention_prevue = ? " +
                "WHERE t.technicien_id = ? GROUP BY t.technicien_id, t.capacite_journaliere_heures";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, technicienId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Technicien introuvable.");
                }
                BigDecimal charge = rs.getBigDecimal("charge");
                BigDecimal capacite = rs.getBigDecimal("capacite_journaliere_heures");
                if (charge.add(nouvelleDuree).compareTo(capacite) > 0) {
                    throw new IllegalStateException("Conflit : capacité journalière dépassée (" +
                            charge.stripTrailingZeros().toPlainString() + " h déjà planifiées + " +
                            nouvelleDuree.stripTrailingZeros().toPlainString() + " h demandées, limite " +
                            capacite.stripTrailingZeros().toPlainString() + " h).");
                }
            }
        }
    }

    private int creerPlanification(Connection c, int responsableId, LocalDate date) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO PLANIFICATIONS (responsable_id, date_planifiee, statut) VALUES (?, ?, 'validated')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, responsableId);
            ps.setDate(2, Date.valueOf(date));
            ps.executeUpdate();
            return generatedId(ps, "planification");
        }
    }

    private BigDecimal calculerScore(Demande d, LocalDate date) {
        long joursAttente = java.time.temporal.ChronoUnit.DAYS.between(
                d.dateSignalement.toLocalDateTime().toLocalDate(), date);
        return BigDecimal.valueOf(poids(d.priorite)).multiply(BigDecimal.valueOf(1L + joursAttente));
    }

    private int creerAffectation(Connection c, int planificationId, Demande d,
                                 int technicienId, LocalDate date, BigDecimal score) throws SQLException {
        String sql = "INSERT INTO AFFECTATIONS (planification_id, demande_id, technicien_id, " +
                "date_intervention_prevue, duree_estimee_heures, score_demande, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'validated')";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, planificationId);
            ps.setInt(2, d.id);
            ps.setInt(3, technicienId);
            ps.setDate(4, Date.valueOf(date));
            ps.setBigDecimal(5, d.dureeEstimee);
            ps.setBigDecimal(6, score);
            ps.executeUpdate();
            return generatedId(ps, "affectation");
        }
    }

    private void enregistrerScorePlanification(Connection c, int planificationId, BigDecimal score)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE PLANIFICATIONS SET score_total = ? WHERE planification_id = ?")) {
            ps.setBigDecimal(1, score);
            ps.setInt(2, planificationId);
            ps.executeUpdate();
        }
    }

    private void marquerDemandeAssignee(Connection c, int demandeId) throws SQLException {
        changerStatutDemande(c, demandeId, "open", "assigned");
    }

    private void changerStatutDemande(Connection c, int demandeId, String ancien, String nouveau)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE DEMANDES SET statut = ? WHERE demande_id = ? AND statut = ?")) {
            ps.setString(1, nouveau);
            ps.setInt(2, demandeId);
            ps.setString(3, ancien);
            if (ps.executeUpdate() != 1) {
                throw new IllegalStateException("Le statut de la demande a changé ; opération refusée.");
            }
        }
    }

    private InterventionContexte lireContexteIntervention(Connection c, int interventionId)
            throws SQLException {
        String sql = "SELECT i.intervention_id, i.date_debut_reelle, d.demande_id, d.statut, " +
                "u.utilisateur_id AS technicien_utilisateur_id " +
                "FROM INTERVENTIONS i JOIN AFFECTATIONS a ON a.affectation_id = i.affectation_id " +
                "JOIN DEMANDES d ON d.demande_id = a.demande_id " +
                "JOIN TECHNICIENS t ON t.technicien_id = a.technicien_id " +
                "JOIN UTILISATEURS u ON u.utilisateur_id = t.utilisateur_id " +
                "WHERE i.intervention_id = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, interventionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Intervention introuvable.");
                }
                InterventionContexte x = new InterventionContexte();
                x.demandeId = rs.getInt("demande_id");
                x.statutDemande = rs.getString("statut");
                x.utilisateurTechnicienId = rs.getInt("technicien_utilisateur_id");
                x.dateDebut = rs.getTimestamp("date_debut_reelle");
                return x;
            }
        }
    }

    private int generatedId(PreparedStatement ps, String nom) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) {
                throw new SQLException("La base n'a pas retourné l'identifiant " + nom + ".");
            }
            return rs.getInt(1);
        }
    }

    private int poids(String priorite) {
        if ("critical".equals(priorite)) return 8;
        if ("high".equals(priorite)) return 4;
        if ("normal".equals(priorite)) return 2;
        if ("low".equals(priorite)) return 1;
        throw new IllegalArgumentException("Priorité inconnue : " + priorite);
    }

    private boolean vide(String texte) { return texte == null || texte.trim().isEmpty(); }

    private static final class Demande {
        int id;
        int competenceId;
        BigDecimal dureeEstimee;
        String statut;
        String priorite;
        Timestamp dateSignalement;
    }

    private static final class InterventionContexte {
        int demandeId;
        int utilisateurTechnicienId;
        String statutDemande;
        Timestamp dateDebut;
    }
}
