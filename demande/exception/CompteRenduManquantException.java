package tn.enetcom.maintenance.demande.exception;

public class CompteRenduManquantException extends RuntimeException {
    public CompteRenduManquantException() {
        super("Un compte rendu est obligatoire pour terminer l'intervention " +
              "(cause de la panne, action réalisée, durée réelle, date de fin).");
    }
}