package maintenance.utilisateur.controller;

import maintenance.utilisateur.dao.UserDAO;
import maintenance.utilisateur.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/app/admin/utilisateurs")
public class UserAdminServlet extends HttpServlet {
	  private static final long serialVersionUID = 1L;

    private static final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<User> users = userDAO.findAll();
            request.setAttribute("users", users);
            request.getRequestDispatcher("/WEB-INF/views/utilisateurs.jsp")
                   .forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Impossible de charger les utilisateurs", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String nom = trim(request.getParameter("nom"));
        String prenom = trim(request.getParameter("prenom"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String role = trim(request.getParameter("role"));

        if (nom.isBlank() || prenom.isBlank() || email.isBlank()
                || password == null || password.length() < 10
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || !List.of("requester", "technician", "maintenance_manager").contains(role)) {
            request.setAttribute("error",
                    "Vérifie les champs. Le mot de passe doit contenir au moins 10 caractères.");
            doGet(request, response);
            return;
        }

        try {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            userDAO.create(nom, prenom, email, hash, role);
            response.sendRedirect(
                    request.getContextPath() + "/app/admin/utilisateurs?created=1");
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                request.setAttribute("error", "Cet email est déjà utilisé.");
                doGet(request, response);
            } else {
                throw new ServletException("Impossible de créer l'utilisateur", e);
            }
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}