package tn.enetcom.maintenance.equipement.model;

import java.util.Objects;

public class Equipement {
    private int id;
    private String designation;
    private String lieu;

    public Equipement() {}

    public Equipement(int id, String designation, String lieu) {
        this.id = id;
        this.designation = designation;
        this.lieu = lieu;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipement)) return false;
        return id == ((Equipement) o).id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Equipement{id=" + id + ", designation='" + designation + "', lieu='" + lieu + "'}";
    }
}