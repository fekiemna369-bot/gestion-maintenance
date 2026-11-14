package tn.enetcom.maintenance.demande.dao;

import tn.enetcom.maintenance.common.Database;
import tn.enetcom.maintenance.demande.model.Intervention;

import java.sql.*;

public class InterventionDAOImpl implements InterventionDAO {

    @Override
    public Intervention findByAffectation(int affectationId) throws SQLException {
        String sql = "SELECT intervention_id, affectation_id, date_debut_reelle, date_fin_reelle, " +
                     "duree_reelle_heures, cause_panne, action_realisee " +
                     "FROM INTERVENTIONS WHERE affectation_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, affectationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        }
    }

    @Override
    public int insert(Intervention i) throws SQLException {
        String sql = "INSERT INTO INTERVENTIONS " +
            "(affectation_id, date_debut_reelle, date_fin_reelle, duree_reelle_heures, " +
            "cause_panne, action_realisee) VALUES (?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getAffectationId());
            ps.setTimestamp(2, i.getDateDebutReelle() == null ? null : Timestamp.valueOf(i.getDateDebutReelle()));
            ps.setTimestamp(3, i.getDateFinReelle()   == null ? null : Timestamp.valueOf(i.getDateFinReelle()));
            ps.setDouble(4, i.getDureeReelleHeures());
            ps.setString(5, i.getCausePanne());
            ps.setString(6, i.getActionRealisee());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Échec insertion intervention, aucun id généré.");
    }

    private Intervention mapper(ResultSet rs) throws SQLException {
        Intervention i = new Intervention();
        i.setId(rs.getInt("intervention_id"));
        i.setAffectationId(rs.getInt("affectation_id"));
        Timestamp deb = rs.getTimestamp("date_debut_reelle");
        Timestamp fin = rs.getTimestamp("date_fin_reelle");
        if (deb != null) i.setDateDebutReelle(deb.toLocalDateTime());
        if (fin != null) i.setDateFinReelle(fin.toLocalDateTime());
        i.setDureeReelleHeures(rs.getDouble("duree_reelle_heures"));
        i.setCausePanne(rs.getString("cause_panne"));
        i.setActionRealisee(rs.getString("action_realisee"));
        return i;
    }
}