package maintenance.utilisateur.service;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {
        // Classe utilitaire : pas d'instance à créer
    }

    // À appeler lors de la création d'un compte.
    public static String hacherMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }

        return BCrypt.hashpw(motDePasse, BCrypt.gensalt(12));
    }

    // À appeler lors de la connexion.
    public static boolean verifierMotDePasse(
            String motDePasseSaisi,
            String hashEnregistre) {

        if (motDePasseSaisi == null || hashEnregistre == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(motDePasseSaisi, hashEnregistre);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}