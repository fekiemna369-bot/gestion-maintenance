package com.maintenance.technician.model;

/**
 * A competence (table COMPETENCES): reseau, materiel or logiciel.
 * The category of a request is the skill required to handle it.
 */
public class Skill {

    public static final String RESEAU = "reseau";
    public static final String MATERIEL = "materiel";
    public static final String LOGICIEL = "logiciel";

    private int id;
    private String name;


    public Skill(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String toString() {
        return name;
    }
}
