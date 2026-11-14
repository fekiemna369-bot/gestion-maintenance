package tn.enetcom.maintenance.demande.controller;

import tn.enetcom.maintenance.common.AuthGuard;
import tn.enetcom.maintenance.common.JsonUtil;
import tn.enetcom.maintenance.demande.dao.DemandeDAOImpl;
import tn.enetcom.maintenance.demande.exception.*;
import tn.enetcom.maintenance.demande.model.Demande;
import tn.enetcom.maintenance.demande.service.DemandeService;

import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Routes exposées (mappings dans web.xml) :
 *
 *  GET    /demandes                    → liste (filtrée selon rôle)
 *  GET    /demandes/{id}               → détail
 *  POST   /demandes                    → création
 *  POST   /demandes/{id}/demarrer      → ASSIGNED → IN_PROGRESS
 *  POST   /demandes/{id}/terminer      → IN_PROGRESS → COMPLETED (compte rendu requis)
 *  POST   /demandes/{id}/annuler       → annulation selon rôle et statut
 */
public class DemandeController extends HttpServlet {

    private final DemandeService service = new DemandeService(new DemandeDAOImpl());

    private static final Pattern DETAIL_PATTERN =
        Pattern.compile("^/(\\d+)$");
    private static final Pattern ACTION_PATTERN =
        Pattern.compile("^/(\\d+)/(demarrer|terminer|annuler|assigner)$");

    // ================================================================
    //  GET
    // ================================================================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String role = (String) sessionAttr(req, "role");
        if (role == null) { resp.sendError(401, "Session requise."); return; }

        String path = req.getPathInfo();

        try {
            // GET /demandes  →  liste filtrée selon rôle
            if (path == null || "/".equals(path)) {
                List<Demande> liste;
                if ("DEMANDEUR".equals(role)) {
                    Integer demandeurId = (Integer) sessionAttr(req, "utilisateurId");
                    if (demandeurId == null) { resp.sendError(401); return; }
                    liste = service.listerParDemandeur(demandeurId);
                } else {
                    liste = service.listerToutes();
                }
                resp.getWriter().write(JsonUtil.demandesToJson(liste));
                return;
            }

            // GET /demandes/{id}
            Matcher m = DETAIL_PATTERN.matcher(path);
            if (m.matches()) {
                Demande d = service.get(Integer.parseInt(m.group(1)));
                resp.getWriter().write(JsonUtil.demandeToJson(d));
                return;
            }

            resp.sendError(404);

        } catch (DemandeNotFoundException ex) {
            resp.sendError(404, ex.getMessage());
        } catch (Exception ex) {
            resp.sendError(500, ex.getMessage());
        }
    }

    // ================================================================
    //  POST
    // ================================================================

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String role = (String) sessionAttr(req, "role");
        if (role == null) { resp.sendError(401, "Session requise."); return; }

        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();

        try {
            // POST /demandes  →  création
            if ("/".equals(path)) {
                if (!"DEMANDEUR".equals(role)) {
                    resp.sendError(403, "Seul un demandeur peut créer une demande.");
                    return;
                }
                Integer demandeurId = (Integer) sessionAttr(req, "utilisateurId");
                if (demandeurId == null) { resp.sendError(401); return; }

                String body = lireCorps(req);
                int equipementId = extraireInt(body, "equipementId");
                int competenceId = extraireInt(body, "competenceId");
                String description = extraireString(body, "description");
                Demande.Priorite priorite = Demande.Priorite.valueOf(
                    extraireString(body, "priorite").toUpperCase());
                double duree = extraireDouble(body, "dureeEstimeeHeures");
                LocalDateTime dateSignalement = LocalDateTime.parse(
                    extraireString(body, "dateSignalement"));

                Demande d = service.creer(demandeurId, equipementId, competenceId,
                                          description, priorite, duree, dateSignalement);
                resp.setStatus(201);
                resp.getWriter().write(JsonUtil.demandeToJson(d));
                return;
            }

            // POST /demandes/{id}/{action}
            Matcher m = ACTION_PATTERN.matcher(path);
            if (m.matches()) {
                int id = Integer.parseInt(m.group(1));
                String action = m.group(2);

                switch (action) {
                    case "assigner" -> {
                        Demande d = service.assignerDev(id);
                        resp.getWriter().write(JsonUtil.demandeToJson(d));
                    }
                    case "demarrer" -> {
                        // Seul le technicien affecté peut démarrer (règle complète
                        // viendra avec la table AFFECTATIONS, ici on reste simple)
                        Demande d = service.demarrer(id);
                        resp.getWriter().write(JsonUtil.demandeToJson(d));
                    }
                    case "terminer" -> {
                        String body = lireCorps(req);
                        String cause = extraireString(body, "causePanne");
                        String actionRealisee = extraireString(body, "actionRealisee");
                        double dureeReelle = extraireDouble(body, "dureeReelleHeures");
                        LocalDateTime dateFin = LocalDateTime.parse(extraireString(body, "dateFin"));
                        Demande d = service.terminer(id, cause, actionRealisee, dureeReelle, dateFin);
                        resp.getWriter().write(JsonUtil.demandeToJson(d));
                    }
                    case "annuler" -> {
                        Demande d = service.annuler(id, role);
                        resp.getWriter().write(JsonUtil.demandeToJson(d));
                    }
                }
                return;
            }

            resp.sendError(404);

        } catch (ValidationException ex) {
            resp.sendError(400, ex.getMessage());
        } catch (CompteRenduManquantException ex) {
            resp.sendError(400, ex.getMessage());
        } catch (TransitionInterditeException ex) {
            resp.sendError(409, ex.getMessage());
        } catch (DemandeNotFoundException ex) {
            resp.sendError(404, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            resp.sendError(400, "Champ invalide : " + ex.getMessage());
        } catch (Exception ex) {
            resp.sendError(500, ex.getMessage());
        }
    }

        // ================================================================
    //  UTILITAIRES
    // ================================================================

    private Object sessionAttr(HttpServletRequest req, String name) {
        HttpSession s = req.getSession(false);
        return s == null ? null : s.getAttribute(name);
    }

    private String lireCorps(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String l;
            while ((l = br.readLine()) != null) sb.append(l);
        }
        return sb.toString();
    }

    /** Cherche la valeur associée à une clé JSON (string ou nombre). */
    private String extraireValeur(String json, String cle) {
        if (json == null) return null;
        String token = "\"" + cle + "\"";
        int i = json.indexOf(token);
        if (i < 0) return null;
        int c = json.indexOf(':', i + token.length());
        if (c < 0) return null;
        int p = c + 1;
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
        if (p >= json.length()) return null;

        // Si la valeur est une chaîne, elle commence par ".
        if (json.charAt(p) == '"') {
            int q2 = json.indexOf('"', p + 1);
            if (q2 < 0) return null;
            return json.substring(p + 1, q2);
        }
        // Sinon, c'est un nombre : on lit jusqu'à , ou }
        int fin = p;
        while (fin < json.length() && ",}\n\r\t ".indexOf(json.charAt(fin)) < 0) fin++;
        return json.substring(p, fin).trim();
    }

    private String extraireString(String json, String cle) {
        return extraireValeur(json, cle);
    }

    private int extraireInt(String json, String cle) {
        String v = extraireValeur(json, cle);
        if (v == null) throw new IllegalArgumentException(cle + " manquant");
        return Integer.parseInt(v.trim());
    }

    private double extraireDouble(String json, String cle) {
        String v = extraireValeur(json, cle);
        if (v == null) throw new IllegalArgumentException(cle + " manquant");
        return Double.parseDouble(v.trim());
    }
}