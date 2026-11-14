package com.gestionmaintenance.intervention;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Java representation of the INTERVENTIONS table. */
public class Intervention {
    private Integer interventionId;
    private Integer affectationId;
    private LocalDateTime dateDebutReelle;
    private LocalDateTime dateFinReelle;
    private BigDecimal dureeReelleHeures;
    private String causePanne;
    private String actionRealisee;
    private LocalDateTime dateCreation;

    public Integer getInterventionId() { return interventionId; }
    public void setInterventionId(Integer interventionId) { this.interventionId = interventionId; }
    public Integer getAffectationId() { return affectationId; }
    public void setAffectationId(Integer affectationId) { this.affectationId = affectationId; }
    public LocalDateTime getDateDebutReelle() { return dateDebutReelle; }
    public void setDateDebutReelle(LocalDateTime dateDebutReelle) { this.dateDebutReelle = dateDebutReelle; }
    public LocalDateTime getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDateTime dateFinReelle) { this.dateFinReelle = dateFinReelle; }
    public BigDecimal getDureeReelleHeures() { return dureeReelleHeures; }
    public void setDureeReelleHeures(BigDecimal dureeReelleHeures) { this.dureeReelleHeures = dureeReelleHeures; }
    public String getCausePanne() { return causePanne; }
    public void setCausePanne(String causePanne) { this.causePanne = causePanne; }
    public String getActionRealisee() { return actionRealisee; }
    public void setActionRealisee(String actionRealisee) { this.actionRealisee = actionRealisee; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
