package com.gestionmaintenance.intervention;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Small command-line entry point for demonstrating the module. */
public class Main {
    public static void main(String[] args) {
        try {
            AffectationInterventionService service = new AffectationInterventionService();

            // Legacy form: <managerId> <requestId> <technicianId> <yyyy-MM-dd>
            if (args.length == 4) {
                int affectationId = service.affecterDemande(
                        Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]), LocalDate.parse(args[3]));
                int interventionId = service.trouverInterventionIdParAffectation(affectationId);
                System.out.println("Affectation créée : affectationId=" + affectationId
                        + ", interventionId=" + interventionId);
                return;
            }

            if (args.length == 3 && "demarrer".equalsIgnoreCase(args[0])) {
                service.demarrer(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                System.out.println("Intervention démarrée.");
                return;
            }

            if (args.length == 6 && "terminer".equalsIgnoreCase(args[0])) {
                service.terminer(Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                        args[3], args[4], new BigDecimal(args[5]));
                System.out.println("Intervention terminée et compte rendu enregistré.");
                return;
            }

            System.out.println("Utilisation :\n"
                    + "  Main <idResponsable> <idDemande> <idTechnicien> <AAAA-MM-JJ>\n"
                    + "  Main demarrer <idUtilisateurTechnicien> <idIntervention>\n"
                    + "  Main terminer <idUtilisateurTechnicien> <idIntervention> <cause> <action> <heures>");
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
