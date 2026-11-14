package tn.enetcom.maintenance.equipement.service;

import tn.enetcom.maintenance.equipement.dao.EquipementDAO;
import tn.enetcom.maintenance.equipement.exception.EquipementDejaUtiliseException;
import tn.enetcom.maintenance.equipement.exception.EquipementNotFoundException;
import tn.enetcom.maintenance.equipement.exception.ValidationException;
import tn.enetcom.maintenance.equipement.model.Equipement;

import java.sql.SQLException;
import java.util.List;

public class EquipementService {

    private final EquipementDAO dao;

    public EquipementService(EquipementDAO dao) {
        this.dao = dao;
    }

    public Equipement creer(String designation, String lieu) throws SQLException {
        valider(designation, lieu);
        designation = designation.trim();
        lieu = lieu.trim();

        if (dao.existeParDesignationEtLieu(designation, lieu))
            throw new ValidationException(
                "Un équipement nommé « " + designation + " » existe déjà dans le lieu « " + lieu + " ».");

        Equipement e = new Equipement(0, designation, lieu);
        int id = dao.insert(e);
        e.setId(id);
        return e;
    }

    public Equipement modifier(int id, String designation, String lieu) throws SQLException {
        Equipement existant = dao.findById(id);
        if (existant == null) throw new EquipementNotFoundException(id);

        valider(designation, lieu);
        designation = designation.trim();
        lieu = lieu.trim();

        boolean change = !designation.equals(existant.getDesignation())
                      || !lieu.equals(existant.getLieu());
        if (change && dao.existeParDesignationEtLieu(designation, lieu))
            throw new ValidationException(
                "Un autre équipement porte déjà ce nom dans ce lieu.");

        existant.setDesignation(designation);
        existant.setLieu(lieu);
        if (!dao.update(existant)) throw new EquipementNotFoundException(id);
        return existant;
    }

    public void supprimer(int id) throws SQLException {
        if (dao.findById(id) == null) throw new EquipementNotFoundException(id);
        int nb = dao.compterDemandesLiees(id);
        if (nb > 0)
            throw new EquipementDejaUtiliseException(id);
        if (!dao.delete(id)) throw new EquipementNotFoundException(id);
    }

    public Equipement get(int id) throws SQLException {
        Equipement e = dao.findById(id);
        if (e == null) throw new EquipementNotFoundException(id);
        return e;
    }

    public List<Equipement> listerTous() throws SQLException {
        return dao.findAll();
    }

    public List<Equipement> listerParLieu(String lieu) throws SQLException {
        if (lieu == null || lieu.isBlank()) return dao.findAll();
        return dao.findByLieu(lieu.trim());
    }

    private void valider(String designation, String lieu) {
        if (designation == null || designation.isBlank())
            throw new ValidationException("La désignation est obligatoire.");
        if (designation.length() > 150)
            throw new ValidationException("La désignation ne doit pas dépasser 150 caractères.");
        if (lieu == null || lieu.isBlank())
            throw new ValidationException("Le lieu est obligatoire.");
        if (lieu.length() > 100)
            throw new ValidationException("Le lieu ne doit pas dépasser 100 caractères.");
    }
}