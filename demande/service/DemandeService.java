package tn.enetcom.maintenance.demande.service;

import tn.enetcom.maintenance.demande.dao.DemandeDAO;
import tn.enetcom.maintenance.demande.exception.CompteRenduManquantException;
import tn.enetcom.maintenance.demande.exception.DemandeNotFoundException;
import tn.enetcom.maintenance.demande.exception.TransitionInterditeException;
import tn.enetcom.maintenance.demande.exception.ValidationException;
import tn.enetcom.maintenance.demande.model.Demande;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DemandeService {

    private final DemandeDAO dao;

    public DemandeService(DemandeDAO dao) {
        this.dao = dao;
    }

    // ================================================================
    //  CRÉATION
    // ================================================================

    public Demande creer(int demandeurId,
                         int equipementId,
                         int competenceId,
                         String description,
                         Demande.Priorite priorite,
                         double dureeEstimeeHeures,
                         LocalDateTime dateSignalement) throws SQLException {

        if (demandeurId <= 0)
            throw new ValidationException("Le demandeur est obligatoire.");
        if (equipementId <= 0)
            throw new ValidationException("L'équipement est obligatoire.");
        if (competenceId <= 0)
            throw new ValidationException("La compétence (catégorie) est obligatoire.");
        if (priorite == null)
            throw new ValidationException("La priorité est obligatoire.");
        if (dateSignalement == null)
            throw new ValidationException("La date de signalement est obligatoire.");

        validerDuree(dureeEstimeeHeures);

        Demande d = new Demande();
        d.setDemandeurId(demandeurId);
        d.setEquipementId(equipementId);
        d.setCompetenceId(competenceId);
        d.setDescription(description);
        d.setPriorite(priorite);
        d.setDureeEstimeeHeures(dureeEstimeeHeures);
        d.setStatut(Demande.Statut.OPEN);
        d.setDateSignalement(dateSignalement);

        int id = dao.insert(d);
        d.setId(id);
        return d;
    }

    // ================================================================
    //  MODIFICATION
    // ================================================================

    public Demande modifier(int id,
                            int equipementId,
                            int competenceId,
                            String description,
                            Demande.Priorite priorite,
                            double dureeEstimeeHeures,
                            LocalDateTime dateSignalement) throws SQLException {

        Demande existante = dao.findById(id);
        if (existante == null) throw new DemandeNotFoundException(id);

        if (existante.getStatut() == Demande.Statut.COMPLETED
         || existante.getStatut() == Demande.Statut.CANCELLED)
            throw new TransitionInterditeException(
                "Impossible de modifier une demande " +
                (existante.getStatut() == Demande.Statut.COMPLETED ? "terminée" : "annulée") + ".");

        if (equipementId <= 0)
            throw new ValidationException("L'équipement est obligatoire.");
        if (competenceId <= 0)
            throw new ValidationException("La compétence est obligatoire.");
        if (priorite == null)
            throw new ValidationException("La priorité est obligatoire.");
        if (dateSignalement == null)
            throw new ValidationException("La date de signalement est obligatoire.");

        validerDuree(dureeEstimeeHeures);

        existante.setEquipementId(equipementId);
        existante.setCompetenceId(competenceId);
        existante.setDescription(description);
        existante.setPriorite(priorite);
        existante.setDureeEstimeeHeures(dureeEstimeeHeures);
        existante.setDateSignalement(dateSignalement);

        if (!dao.update(existante)) throw new DemandeNotFoundException(id);
        return existante;
    }

    // ================================================================
    //  STATUTS & ANNULATION
    // ================================================================

    public Demande annuler(int id, String role) throws SQLException {
        Demande d = dao.findById(id);
        if (d == null) throw new DemandeNotFoundException(id);

        Demande.Statut s = d.getStatut();
        if (s == Demande.Statut.COMPLETED)
            throw new TransitionInterditeException("Une demande terminée ne peut pas être annulée.");
        if (s == Demande.Statut.CANCELLED)
            throw new TransitionInterditeException("La demande est déjà annulée.");

        switch (role) {
            case "DEMANDEUR":
                if (s != Demande.Statut.OPEN && s != Demande.Statut.ASSIGNED)
                    throw new TransitionInterditeException(
                        "Un demandeur ne peut annuler qu'une demande ouverte ou assignée.");
                break;
            case "RESPONSABLE":
                break;
            default:
                throw new TransitionInterditeException(
                    "Rôle non autorisé à annuler une demande : " + role);
        }

        if (!dao.updateStatut(id, Demande.Statut.CANCELLED))
            throw new DemandeNotFoundException(id);
        d.setStatut(Demande.Statut.CANCELLED);
        return d;
    }

    public Demande changerStatut(int id, Demande.Statut cible) throws SQLException {
        Demande d = dao.findById(id);
        if (d == null) throw new DemandeNotFoundException(id);

        verifierTransition(d.getStatut(), cible);

        if (!dao.updateStatut(id, cible))
            throw new DemandeNotFoundException(id);
        d.setStatut(cible);
        return d;
    }

    private void verifierTransition(Demande.Statut actuel, Demande.Statut cible) {
        boolean ok = switch (actuel) {
            case OPEN        -> cible == Demande.Statut.ASSIGNED;
            case ASSIGNED    -> cible == Demande.Statut.IN_PROGRESS;
            case IN_PROGRESS -> cible == Demande.Statut.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!ok)
            throw new TransitionInterditeException(
                "Transition interdite : " + actuel + " → " + cible + ".");
    }

    // ================================================================
    //  DÉMARRAGE ET FIN D'INTERVENTION
    // ================================================================

    public Demande demarrer(int id) throws SQLException {
        Demande d = dao.findById(id);
        if (d == null) throw new DemandeNotFoundException(id);
        if (d.getStatut() != Demande.Statut.ASSIGNED)
            throw new TransitionInterditeException(
                "Seule une demande assignée peut être démarrée (statut actuel : " + d.getStatut() + ").");
        if (!dao.updateStatut(id, Demande.Statut.IN_PROGRESS))
            throw new DemandeNotFoundException(id);
        d.setStatut(Demande.Statut.IN_PROGRESS);
        return d;
    }

    public Demande terminer(int id,
                            String causePanne,
                            String actionRealisee,
                            double dureeReelleHeures,
                            LocalDateTime dateFin) throws SQLException {

        Demande d = dao.findById(id);
        if (d == null) throw new DemandeNotFoundException(id);
        if (d.getStatut() != Demande.Statut.IN_PROGRESS)
            throw new TransitionInterditeException(
                "Seule une demande en cours peut être terminée (statut actuel : " + d.getStatut() + ").");

        if (causePanne == null || causePanne.isBlank()
         || actionRealisee == null || actionRealisee.isBlank()
         || dureeReelleHeures <= 0
         || dateFin == null)
            throw new CompteRenduManquantException();

        if (!dao.updateStatut(id, Demande.Statut.COMPLETED))
            throw new DemandeNotFoundException(id);
        d.setStatut(Demande.Statut.COMPLETED);
        return d;
    }

    // ================================================================
    //  LECTURE
    // ================================================================

    public Demande get(int id) throws SQLException {
        Demande d = dao.findById(id);
        if (d == null) throw new DemandeNotFoundException(id);
        return d;
    }

    public List<Demande> listerToutes() throws SQLException {
        return dao.findAll();
    }

    public List<Demande> listerParDemandeur(int demandeurId) throws SQLException {
        return dao.findByDemandeur(demandeurId);
    }

    public List<Demande> listerParStatut(Demande.Statut statut) throws SQLException {
        return dao.findByStatut(statut);
    }

    // ================================================================
    //  VALIDATION
    // ================================================================

    private void validerDuree(double duree) {
        if (duree < 1 || duree > 8)
            throw new ValidationException(
                "La durée estimée doit être comprise entre 1 et 8 heures (reçu : " + duree + " h).");
    }
    /** ⚠️ DEV UNIQUEMENT : force le passage OPEN → ASSIGNED (M5 fera ça proprement). */
    public Demande assignerDev(int id) throws SQLException {
        Demande d = dao.findById(id);
        if (d == null) throw new DemandeNotFoundException(id);
        if (d.getStatut() != Demande.Statut.OPEN)
            throw new TransitionInterditeException(
                "Seule une demande ouverte peut être assignée (statut actuel : " + d.getStatut() + ").");
        if (!dao.updateStatut(id, Demande.Statut.ASSIGNED))
            throw new DemandeNotFoundException(id);
        d.setStatut(Demande.Statut.ASSIGNED);
        return d;
    }
}