package tn.enetcom.maintenance.equipement.exception;

public class EquipementNotFoundException extends RuntimeException {
    public EquipementNotFoundException(int id) {
        super("Équipement introuvable : id=" + id);
    }
}