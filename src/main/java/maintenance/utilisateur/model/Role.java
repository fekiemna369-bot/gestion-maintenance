package maintenance.utilisateur.model;

public enum Role {
    REQUESTER("requester"),
    TECHNICIAN("technician"),
    MAINTENANCE_MANAGER("maintenance_manager");

    private final String valeurBaseDeDonnees;

    Role(String valeurBaseDeDonnees) {
        this.valeurBaseDeDonnees = valeurBaseDeDonnees;
    }

    public String getValeurBaseDeDonnees() {
        return valeurBaseDeDonnees;
    }

    public static Role depuisBaseDeDonnees(String valeur) {
        for (Role role : Role.values()) {
            if (role.valeurBaseDeDonnees.equals(valeur)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle inconnu : " + valeur);
    }
}