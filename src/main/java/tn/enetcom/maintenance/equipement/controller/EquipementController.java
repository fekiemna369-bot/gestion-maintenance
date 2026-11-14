package tn.enetcom.maintenance.equipement.controller;

import tn.enetcom.maintenance.common.AuthGuard;
import tn.enetcom.maintenance.common.JsonUtil;
import tn.enetcom.maintenance.equipement.dao.EquipementDAOImpl;
import tn.enetcom.maintenance.equipement.exception.*;
import tn.enetcom.maintenance.equipement.model.Equipement;
import tn.enetcom.maintenance.equipement.service.EquipementService;

import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EquipementController extends HttpServlet {

    private final EquipementService service = new EquipementService(new EquipementDAOImpl());
    private static final Pattern ID_PATTERN = Pattern.compile("^/(\\d+)$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AuthGuard.requireRole(req, resp, "RESPONSABLE")) return;
        resp.setContentType("application/json;charset=UTF-8");

        String path = req.getPathInfo();
        try {
            if (path == null || "/".equals(path)) {
                String lieu = req.getParameter("lieu");
                List<Equipement> liste = service.listerParLieu(lieu);
                resp.getWriter().write(JsonUtil.equipementsToJson(liste));
            } else {
                Matcher m = ID_PATTERN.matcher(path);
                if (!m.matches()) { resp.sendError(404); return; }
                Equipement e = service.get(Integer.parseInt(m.group(1)));
                resp.getWriter().write(JsonUtil.equipementToJson(e));
            }
        } catch (EquipementNotFoundException ex) {
            resp.sendError(404, ex.getMessage());
        } catch (Exception ex) {
            resp.sendError(500, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AuthGuard.requireRole(req, resp, "RESPONSABLE")) return;
        try {
            String[] body = lireCorps(req);
            Equipement e = service.creer(body[0], body[1]);
            resp.setStatus(201);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(JsonUtil.equipementToJson(e));
        } catch (ValidationException ex) {
            resp.sendError(400, ex.getMessage());
        } catch (Exception ex) {
            resp.sendError(500, ex.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AuthGuard.requireRole(req, resp, "RESPONSABLE")) return;
        Matcher m = ID_PATTERN.matcher(req.getPathInfo() == null ? "" : req.getPathInfo());
        if (!m.matches()) { resp.sendError(404); return; }
        try {
            int id = Integer.parseInt(m.group(1));
            String[] body = lireCorps(req);
            Equipement e = service.modifier(id, body[0], body[1]);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(JsonUtil.equipementToJson(e));
        } catch (EquipementNotFoundException ex) {
            resp.sendError(404, ex.getMessage());
        } catch (ValidationException ex) {
            resp.sendError(400, ex.getMessage());
        } catch (Exception ex) {
            resp.sendError(500, ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AuthGuard.requireRole(req, resp, "RESPONSABLE")) return;
        Matcher m = ID_PATTERN.matcher(req.getPathInfo() == null ? "" : req.getPathInfo());
        if (!m.matches()) { resp.sendError(404); return; }
        try {
            service.supprimer(Integer.parseInt(m.group(1)));
            resp.setStatus(204);
        } catch (EquipementNotFoundException ex) {
            resp.sendError(404, ex.getMessage());
        } catch (EquipementDejaUtiliseException ex) {
            resp.sendError(409, ex.getMessage());
        } catch (Exception ex) {
            resp.sendError(500, ex.getMessage());
        }
    }

    private String[] lireCorps(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String l;
            while ((l = br.readLine()) != null) sb.append(l);
        }
        String json = sb.toString();
        return new String[]{ extraire(json, "designation"), extraire(json, "lieu") };
    }

    private String extraire(String json, String cle) {
        String token = "\"" + cle + "\"";
        int i = json.indexOf(token);
        if (i < 0) return null;
        int c = json.indexOf(':', i);
        int q1 = json.indexOf('"', c + 1);
        int q2 = json.indexOf('"', q1 + 1);
        if (q1 < 0 || q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }
}