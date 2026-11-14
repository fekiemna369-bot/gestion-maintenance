package tn.enetcom.maintenance.demande.model;

import java.time.LocalDateTime;

public class Intervention {

    private int id;
    private int affectationId;
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;
    private double dureeReelleHeures;
    private String causePanne;
    private String actionRealisee;

    public Intervention() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAffectationId() { return affectationId; }
    public void setAffectationId(int affectationId) { this.affectationId = affectationId; }

    public LocalDateTime getDateDebutReelle() { return dateDebutReelle; }
    public void setDateDebutReelle(LocalDateTime dateDebutReelle) { this.dateDebutReelle = dateDebutReelle; }

    public LocalDateTime getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDateTime dateFinReelle) { this.dateFinReelle = dateFinReelle; }

    public double getDureeReelleHeures() { return dureeReelleHeures; }
    public void setDureeReelleHeures(double dureeReelleHeures) { this.dureeReelleHeures = dureeReelleHeures; }

    public String getCausePanne() { return causePanne; }
    public void setCausePanne(String causePanne) { this.causePanne = causePanne; }

    public String getActionRealisee() { return actionRealisee; }
    public void setActionRealisee(String actionRealisee) { this.actionRealisee = actionRealisee; }
}