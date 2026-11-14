package tn.enetcom.maintenance.equipement.dao;

import tn.enetcom.maintenance.equipement.model.Equipement;

import java.sql.SQLException;
import java.util.List;

public interface EquipementDAO {

    Equipement findById(int id) throws SQLException;

    List<Equipement> findAll() throws SQLException;

    List<Equipement> findByLieu(String lieu) throws SQLException;

    int insert(Equipement e) throws SQLException;

    boolean update(Equipement e) throws SQLException;

    boolean delete(int id) throws SQLException;

    boolean existeParDesignationEtLieu(String designation, String lieu) throws SQLException;

    int compterDemandesLiees(int idEquipement) throws SQLException;
}