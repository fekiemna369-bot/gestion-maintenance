package tn.enetcom.maintenance.equipement.service;

import org.junit.jupiter.api.Test;
import tn.enetcom.maintenance.equipement.dao.EquipementDAO;
import tn.enetcom.maintenance.equipement.exception.*;
import tn.enetcom.maintenance.equipement.model.Equipement;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EquipementServiceTest {

    static class FakeDAO implements EquipementDAO {
        Map<Integer, Equipement> data = new HashMap<>();
        int seq = 1;
        int demandesLiees = 0;

        public Equipement findById(int id) { return data.get(id); }
        public List<Equipement> findAll() { return new ArrayList<>(data.values()); }
        public List<Equipement> findByLieu(String lieu) {
            List<Equipement> r = new ArrayList<>();
            for (Equipement e : data.values()) if (e.getLieu().equals(lieu)) r.add(e);
            return r;
        }
        public int insert(Equipement e) { e.setId(seq++); data.put(e.getId(), e); return e.getId(); }
        public boolean update(Equipement e) { data.put(e.getId(), e); return true; }
        public boolean delete(int id) { return data.remove(id) != null; }
        public boolean existeParDesignationEtLieu(String d, String l) {
            return data.values().stream().anyMatch(e -> e.getDesignation().equals(d) && e.getLieu().equals(l));
        }
        public int compterDemandesLiees(int id) { return demandesLiees; }
    }

    @Test
    void creation_ok() throws SQLException {
        EquipementService s = new EquipementService(new FakeDAO());
        Equipement e = s.creer("projecteur", "salle A");
        assertEquals("projecteur", e.getDesignation());
        assertEquals("salle A", e.getLieu());
    }

    @Test
    void creation_refusee_si_designation_vide() {
        EquipementService s = new EquipementService(new FakeDAO());
        assertThrows(ValidationException.class, () -> s.creer("", "salle A"));
    }

    @Test
    void creation_refusee_si_doublon() throws SQLException {
        EquipementService s = new EquipementService(new FakeDAO());
        s.creer("projecteur", "salle A");
        assertThrows(ValidationException.class, () -> s.creer("projecteur", "salle A"));
    }

    @Test
    void suppression_refusee_si_demandes_liees() throws SQLException {
        FakeDAO dao = new FakeDAO();
        EquipementService s = new EquipementService(dao);
        Equipement e = s.creer("projecteur", "salle A");
        dao.demandesLiees = 2;
        assertThrows(EquipementDejaUtiliseException.class, () -> s.supprimer(e.getId()));
    }

    @Test
    void modification_du_lieu_puis_retour() throws SQLException {
        EquipementService s = new EquipementService(new FakeDAO());
        Equipement e = s.creer("projecteur", "salle A");
        s.modifier(e.getId(), "projecteur", "salle D");
        assertEquals("salle D", s.get(e.getId()).getLieu());
        s.modifier(e.getId(), "projecteur", "salle A");
        assertEquals("salle A", s.get(e.getId()).getLieu());
    }
}