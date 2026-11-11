package com.maintenance.technician.model;

import java.time.LocalDate;


public class Unavailability {

    private int id;              // indisponibilite_id
    private int technicianId;    // technicien_id
    private Date date;      // date_indisponible
    private String reason;       // motif


    public Unavailability(int id, int technicianId, LocalDate date, String reason) {
        this.id = id;
        this.technicianId = technicianId;
        this.date = date;
        this.reason = reason;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTechnicianId() { return technicianId; }
    public void setTechnicianId(int technicianId) { this.technicianId = technicianId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String toString() {
        return date + " (" + reason + ")";
    }
}
