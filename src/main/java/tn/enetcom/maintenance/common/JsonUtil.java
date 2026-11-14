package tn.enetcom.maintenance.common;

import tn.enetcom.maintenance.equipement.model.Equipement;
import java.util.List;

public final class JsonUtil {
    private JsonUtil() {}

    public static String equipementToJson(Equipement e) {
        return "{\"id\":" + e.getId()
                + ",\"designation\":\"" + esc(e.getDesignation())
                + "\",\"lieu\":\"" + esc(e.getLieu()) + "\"}";
    }

    public static String equipementsToJson(List<Equipement> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(equipementToJson(list.get(i)));
        }
        return sb.append(']').toString();
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }
    // ================================================================
    //  Demande
    // ================================================================

    public static String demandeToJson(tn.enetcom.maintenance.demande.model.Demande d) {
        if (d == null) return "null";
        return "{\"id\":" + d.getId()
             + ",\"demandeurId\":" + d.getDemandeurId()
             + ",\"equipementId\":" + d.getEquipementId()
             + ",\"competenceId\":" + d.getCompetenceId()
             + ",\"description\":\"" + esc(d.getDescription()) + "\""
             + ",\"priorite\":\"" + d.getPriorite().name() + "\""
             + ",\"dureeEstimeeHeures\":" + d.getDureeEstimeeHeures()
             + ",\"statut\":\"" + d.getStatut().name() + "\""
             + ",\"dateSignalement\":\"" + d.getDateSignalement() + "\"}";
    }

    public static String demandesToJson(java.util.List<tn.enetcom.maintenance.demande.model.Demande> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(demandeToJson(list.get(i)));
        }
        return sb.append(']').toString();
    }
}