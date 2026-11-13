package maintenance.utilisateur.service;

import maintenance.utilisateur.dao.UserDAO;
import maintenance.utilisateur.model.User;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public Optional<User> authentifier(String email, String motDePasse)
            throws SQLException {

        if (email == null || email.isBlank()
                || motDePasse == null || motDePasse.isBlank()) {
            return Optional.empty();
        }

        Optional<UserDAO.AuthRecord> resultat =
                userDAO.findForLogin(email.trim().toLowerCase());

        if (resultat.isEmpty()) {
            return Optional.empty();
        }

        UserDAO.AuthRecord authRecord = resultat.get();

        if (!PasswordUtil.verifierMotDePasse(
                motDePasse,
                authRecord.passwordHash())) {
            return Optional.empty();
        }

        return Optional.of(authRecord.user());
    }
}