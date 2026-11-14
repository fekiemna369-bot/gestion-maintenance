package tn.enetcom.maintenance.demande.dao;

import tn.enetcom.maintenance.demande.model.Intervention;

import java.sql.SQLException;

public interface InterventionDAO {
    Intervention findByAffectation(int affectationId) throws SQLException;
    int insert(Intervention i) throws SQLException;
}