package maintenance.utilisateur.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebFilter("/app/*")
public class RoleFilter implements Filter {
    private static final Map<String, String> REQUIRED_ROLE = Map.of(
            "/app/admin/", "maintenance_manager",
            "/app/technicien/", "technician",
            "/app/demandeur/", "requester"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String required = REQUIRED_ROLE.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse(null);
        HttpSession session = request.getSession(false);
        if (required != null && (session == null || !required.equals(session.getAttribute("role")))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès interdit");
            return;
        }
        chain.doFilter(req, res);
    }
}
