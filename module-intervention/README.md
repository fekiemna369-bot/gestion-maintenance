# Module affectation et intervention

Module Java 8 avec JDBC pour le schéma MySQL `gestion_maintenance`.

## Configuration

Configurer les variables d'environnement suivantes avant de lancer l'application :

- `DB_URL` : par exemple `jdbc:mysql://localhost:3306/gestion_maintenance?serverTimezone=UTC`
- `DB_USER` : utilisateur MySQL
- `DB_PASSWORD` : mot de passe MySQL

Le schéma `data.sql` doit être importé dans MySQL avant l'utilisation. Le connecteur MySQL est déclaré dans `pom.xml`.

## Opérations fournies

- `affecterDemande(responsableId, demandeId, technicienId, date)` : vérifie le rôle du responsable, le statut ouvert, la compétence, l'indisponibilité et la capacité quotidienne. Crée une planification validée, une affectation validée, passe la demande à `assigned` et crée l'intervention.
- `trouverInterventionIdParAffectation(affectationId)` : retrouve l'ID d'intervention à partir de l'affectation créée.
- `demarrer(utilisateurTechnicienId, interventionId)` : vérifie que l'utilisateur est le technicien affecté, puis passe la demande à `in_progress` et enregistre le début réel via `InterventionDAO`.
- `terminer(utilisateurTechnicienId, interventionId, cause, action, dureeReelle)` : vérifie le technicien affecté et le compte rendu, enregistre la fin réelle via `InterventionDAO` et passe la demande à `completed`.

`InterventionDAO` et `InterventionDAOImpl` isolent les opérations SQL de la table `INTERVENTIONS` et utilisent la transaction ouverte par le service.

## Démonstration en ligne de commande

Depuis la racine du module, compile toutes les classes, puis utilise les modes suivants :

```cmd
javac -d target\classes src\main\java\com\gestionmaintenance\intervention\*.java
java -cp "target\classes;lib\mysql-connector-j-8.0.33.jar" com.gestionmaintenance.intervention.Main <idResponsable> <idDemande> <idTechnicien> 2026-11-16
java -cp "target\classes;lib\mysql-connector-j-8.0.33.jar" com.gestionmaintenance.intervention.Main demarrer <idUtilisateurTechnicien> <idIntervention>
java -cp "target\classes;lib\mysql-connector-j-8.0.33.jar" com.gestionmaintenance.intervention.Main terminer <idUtilisateurTechnicien> <idIntervention> "cause" "action réalisée" 2
```

Les identifiants utilisateur et technicien sont distincts dans le schéma : l'affectation reçoit `technicien_id`, tandis que démarrer/terminer reçoivent `UTILISATEURS.utilisateur_id` du technicien connecté.

Les erreurs de règle métier sont signalées par `IllegalArgumentException` ou `IllegalStateException`; les erreurs SQL par `SQLException`.
