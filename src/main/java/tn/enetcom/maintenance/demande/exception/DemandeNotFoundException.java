package tn.enetcom.maintenance.demande.exception;

public class DemandeNotFoundException extends RuntimeException {
    public DemandeNotFoundException(int id) {
        super("Demande introuvable : id=" + id);
    }
}