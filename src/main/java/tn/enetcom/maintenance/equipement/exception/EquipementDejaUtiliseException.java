package tn.enetcom.maintenance.equipement.exception;

public class EquipementDejaUtiliseException extends RuntimeException {
    public EquipementDejaUtiliseException(int id) {
        super("Suppression refusée : l'équipement " + id + " est référencé par une ou plusieurs demandes.");
    }
}