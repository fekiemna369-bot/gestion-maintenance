package com.maintenance.technician.service;


public class ConflictException extends RuntimeException {

    public enum Rule {
        COMPETENCE_MANQUANTE,
        TECHNICIEN_INDISPONIBLE,
        CAPACITE_DEPASSEE,
        DONNEE_INVALIDE
    }

    private final Rule rule;

    public ConflictException(Rule rule, String message) {
        super(message);
        this.rule = rule;
    }

    public Rule getRule() {
        return rule;
    }
}
