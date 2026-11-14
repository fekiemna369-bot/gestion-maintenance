package tn.enetcom.maintenance.demande.dao;

import tn.enetcom.maintenance.demande.model.Demande;

import java.sql.SQLException;
import java.util.List;

public interface DemandeDAO {

    Demande findById(int id) throws SQLException;

    List<Demande> findAll() throws SQLException;

    List<Demande> findByDemandeur(int demandeurId) throws SQLException;

    List<Demande> findByStatut(Demande.Statut statut) throws SQLException;

    int insert(Demande d) throws SQLException;

    boolean updateStatut(int id, Demande.Statut statut) throws SQLException;

    boolean update(Demande d) throws SQLException;
}