package maintenance.utilisateur.controller;

import maintenance.utilisateur.dao.UserDAO;
import maintenance.utilisateur.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	  private static final long serialVersionUID = 1L;

    private static final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            request.setAttribute("error", "Email et mot de passe obligatoires.");
            doGet(request, response);
            return;
        }

        try {
            Optional<UserDAO.AuthRecord> record = userDAO.findForLogin(email);

            if (record.isEmpty()
                    || !isValidPassword(password, record.get().passwordHash())) {
                request.setAttribute("error", "Email ou mot de passe incorrect.");
                doGet(request, response);
                return;
            }

            User user = record.get().user();

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("userId", user.getId());
            session.setAttribute("nom", user.getNom());
            session.setAttribute("prenom", user.getPrenom());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("role", user.getRole());

            response.sendRedirect(request.getContextPath() + "/app/profil");

        } catch (SQLException e) {
            throw new ServletException("Erreur d'accès à la base de données", e);
        }
    }

    private static boolean isValidPassword(String password, String passwordHash) {
        try {
            return BCrypt.checkpw(password, passwordHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}