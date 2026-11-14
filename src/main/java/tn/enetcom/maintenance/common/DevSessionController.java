package tn.enetcom.maintenance.common;

import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * ⚠️ SERVLET DE DEV UNIQUEMENT — à supprimer avant la soutenance.
 */
public class DevSessionController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        if (req.getParameter("logout") != null) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
            resp.getWriter().write("{\"logout\":true}");
            return;
        }

        String role = req.getParameter("role");
        String uid  = req.getParameter("utilisateurId");

        if (role == null || uid == null) {
            resp.sendError(400, "Paramètres requis : role et utilisateurId");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("role", role);
        session.setAttribute("utilisateurId", Integer.parseInt(uid));

        resp.getWriter().write(
            "{\"role\":\"" + role + "\",\"utilisateurId\":" + uid + "}"
        );
    }
}