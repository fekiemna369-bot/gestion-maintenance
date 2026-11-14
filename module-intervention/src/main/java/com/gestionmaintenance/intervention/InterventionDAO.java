package com.gestionmaintenance.intervention;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/** Data access operations for INTERVENTIONS. The caller owns the transaction. */
public interface InterventionDAO {
    int inserer(Connection connection, int affectationId) throws SQLException;
    Integer trouverIdParAffectation(Connection connection, int affectationId) throws SQLException;
    Intervention trouverParAffectation(Connection connection, int affectationId) throws SQLException;
    void enregistrerDebut(Connection connection, int interventionId, LocalDateTime dateDebut)
            throws SQLException;
    void enregistrerFin(Connection connection, int interventionId, LocalDateTime dateFin,
                        BigDecimal dureeReelleHeures, String causePanne, String actionRealisee)
            throws SQLException;
}
