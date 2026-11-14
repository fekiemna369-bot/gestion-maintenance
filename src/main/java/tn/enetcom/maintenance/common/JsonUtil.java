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
}