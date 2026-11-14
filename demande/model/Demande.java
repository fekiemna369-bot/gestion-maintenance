package tn.enetcom.maintenance.demande.model;

import java.time.LocalDateTime;

public class Demande {

    public enum Priorite {
        CRITICAL(8, 1),
        HIGH(4, 3),
        NORMAL(2, 7),
        LOW(1, 14);

        private final int poids;
        private final int delaiJours;

        Priorite(int poids, int delaiJours) {
            this.poids = poids;
            this.delaiJours = delaiJours;
        }

        public int getPoids() { return poids; }
        public int getDelaiJours() { return delaiJours; }
    }

    public enum Statut {
        OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    private int id;
    private int demandeurId;
    private int equipementId;
    private int competenceId;
    private String description;
    private Priorite priorite;
    private double dureeEstimeeHeures;
    private Statut statut;
    private LocalDateTime dateSignalement;

    public Demande() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDemandeurId() { return demandeurId; }
    public void setDemandeurId(int demandeurId) { this.demandeurId = demandeurId; }

    public int getEquipementId() { return equipementId; }
    public void setEquipementId(int equipementId) { this.equipementId = equipementId; }

    public int getCompetenceId() { return competenceId; }
    public void setCompetenceId(int competenceId) { this.competenceId = competenceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }

    public double getDureeEstimeeHeures() { return dureeEstimeeHeures; }
    public void setDureeEstimeeHeures(double dureeEstimeeHeures) { this.dureeEstimeeHeures = dureeEstimeeHeures; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }
}