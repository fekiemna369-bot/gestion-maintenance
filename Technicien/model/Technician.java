package com.maintenance.technician.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class Technician {

    private int id;                 // technicien_id
    private int userId;             // utilisateur_id
    private String firstName;       // UTILISATEURS.prenom
    private String lastName;        // UTILISATEURS.nom
    private String email;           // UTILISATEURS.email
    private BigDecimal dailyCapacity = new BigDecimal("8.00"); // capacite_journaliere_heures
    private List<Skill> skills = new ArrayList<>();



    public Technician(int id, int userId, String firstName, String lastName, BigDecimal dailyCapacity) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dailyCapacity = dailyCapacity;
    }

    public boolean hasSkill(String skillName) {
        return skills.stream().anyMatch(s -> s.getName().equalsIgnoreCase(skillName));
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getDailyCapacity() { return dailyCapacity; }
    public void setDailyCapacity(BigDecimal dailyCapacity) { this.dailyCapacity = dailyCapacity; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }

    public String toString() {
        return getFullName() + " " + skills;
    }
}
