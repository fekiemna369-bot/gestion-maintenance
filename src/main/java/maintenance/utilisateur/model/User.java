package maintenance.utilisateur.model ;

public class User {
    private final long id;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String role;
    private final boolean actif;

    public User(long id, String nom, String prenom, String email, String role, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.actif = actif;
    }

    public long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActif() { return actif; }
}
