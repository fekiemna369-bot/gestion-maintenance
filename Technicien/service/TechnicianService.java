package com.maintenance.technician.service;

import com.maintenance.technician.model.Skill;
import com.maintenance.technician.model.Technician;
import com.maintenance.technician.model.Unavailability;
import com.maintenance.technician.repository.TechnicianRepository;
import com.maintenance.technician.service.ConflictException.Rule;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Logique métier du module techniciens.
 *
 * Méthodes utilisées par les autres modules :
 *  - membre 4 (affectation manuelle) : checkCanAssign(...)  -> lève ConflictException avec la règle violée
 *  - membre 5 (algorithme glouton)   : findEligible(...), getRemainingCapacity(...)
 *  - membre 6 (planning)             : getDailyLoad(...), isAvailable(...), getUnavailabilities(...)
 */
public class TechnicianService {

    private static final BigDecimal MAX_CAPACITY = new BigDecimal("8");

    private final TechnicianRepository repo;

    public TechnicianService(TechnicianRepository repo) {
        this.repo = repo;
    }

    // ================================================================ 1. CRUD techniciens

    /** Crée la fiche technicien d'un utilisateur déjà créé par le module 1 (rôle technician). */
    public int createTechnician(int userId, BigDecimal dailyCapacity, Collection<Integer> skillIds)
            throws SQLException {
        BigDecimal capacity = dailyCapacity == null ? MAX_CAPACITY : dailyCapacity;
        validateCapacity(capacity);
        if (repo.findByUserId(userId).isPresent()) {
            throw new ConflictException(Rule.DONNEE_INVALIDE, "Cet utilisateur est déjà enregistré comme technicien.");
        }
        int id = repo.create(userId, capacity);
        if (skillIds != null) repo.replaceSkills(id, skillIds);
        return id;
    }

    public void updateTechnician(int technicianId, BigDecimal dailyCapacity, Collection<Integer> skillIds)
            throws SQLException {
        getTechnician(technicianId);
        if (dailyCapacity != null) {
            validateCapacity(dailyCapacity);
            repo.updateCapacity(technicianId, dailyCapacity);
        }
        if (skillIds != null) repo.replaceSkills(technicianId, skillIds);
    }

    public void deleteTechnician(int technicianId) throws SQLException {
        getTechnician(technicianId);
        if (repo.hasAssignments(technicianId)) {
            throw new ConflictException(Rule.DONNEE_INVALIDE,
                    "Suppression impossible : ce technicien a déjà des interventions. Désactivez plutôt son compte.");
        }
        repo.delete(technicianId);
    }

    public Technician getTechnician(int technicianId) throws SQLException {
        return repo.findById(technicianId).orElseThrow(() ->
                new ConflictException(Rule.DONNEE_INVALIDE, "Technicien introuvable (id " + technicianId + ")."));
    }

    public Optional<Technician> getTechnicianByUserId(int userId) throws SQLException {
        return repo.findByUserId(userId);
    }

    public List<Technician> getAllTechnicians() throws SQLException {
        return repo.findAll();
    }

    // ================================================================ 2-3. compétences

    public List<Skill> getAllSkills() throws SQLException {
        return repo.findAllSkills();
    }

    public void addSkill(int technicianId, int skillId) throws SQLException {
        getTechnician(technicianId);
        repo.addSkill(technicianId, skillId);
    }

    public void removeSkill(int technicianId, int skillId) throws SQLException {
        repo.removeSkill(technicianId, skillId);
    }

    /** skillName = "reseau", "materiel" ou "logiciel" (= COMPETENCES.nom). */
    public boolean hasSkill(int technicianId, String skillName) throws SQLException {
        return getTechnician(technicianId).hasSkill(skillName);
    }

    // ================================================================ 4-5. indisponibilités

    /**
     * Déclare une période d'indisponibilité (du ... au ... inclus) : une ligne par jour en base.
     * Retourne les jours de la période où le technicien avait déjà des interventions,
     * pour prévenir le responsable (ou lancer la replanification si l'équipe prend cette option).
     */
    public List<LocalDate> declareUnavailability(int technicianId, LocalDate from, LocalDate to, String reason)
            throws SQLException {
        getTechnician(technicianId);
        if (from == null) throw new ConflictException(Rule.DONNEE_INVALIDE, "La date de début est obligatoire.");
        if (to == null) to = from;
        if (to.isBefore(from)) {
            throw new ConflictException(Rule.DONNEE_INVALIDE, "La date de fin est avant la date de début.");
        }
        if (reason == null || reason.isBlank()) {
            throw new ConflictException(Rule.DONNEE_INVALIDE, "Le motif est obligatoire (congé, formation...).");
        }
        List<LocalDate> daysWithWork = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            repo.addUnavailability(technicianId, d, reason.trim());
            if (repo.sumDailyLoad(technicianId, d).signum() > 0) daysWithWork.add(d);
        }
        return daysWithWork;
    }

    public void deleteUnavailability(int unavailabilityId) throws SQLException {
        repo.deleteUnavailability(unavailabilityId);
    }

    public List<Unavailability> getUnavailabilities(int technicianId, LocalDate from, LocalDate to)
            throws SQLException {
        return repo.findUnavailabilities(technicianId, from, to);
    }

    public boolean isAvailable(int technicianId, LocalDate date) throws SQLException {
        return !repo.isUnavailable(technicianId, date);
    }

    // ================================================================ 6-7. charge et capacité

    /** Heures déjà affectées au technicien ce jour-là. */
    public BigDecimal getDailyLoad(int technicianId, LocalDate date) throws SQLException {
        return repo.sumDailyLoad(technicianId, date);
    }

    /** Heures encore libres ce jour-là (0 si indisponible). */
    public BigDecimal getRemainingCapacity(int technicianId, LocalDate date) throws SQLException {
        if (!isAvailable(technicianId, date)) return BigDecimal.ZERO;
        Technician t = getTechnician(technicianId);
        BigDecimal remaining = t.getDailyCapacity().subtract(getDailyLoad(technicianId, date));
        return remaining.max(BigDecimal.ZERO);
    }

    /** Charge de tous les techniciens pour un jour (écran "Consultation de la charge"). */
    public List<DailyLoad> getLoadsForDay(LocalDate date) throws SQLException {
        List<DailyLoad> result = new ArrayList<>();
        for (Technician t : repo.findAll()) {
            boolean available = isAvailable(t.getId(), date);
            result.add(new DailyLoad(t, date, getDailyLoad(t.getId(), date), t.getDailyCapacity(), available));
        }
        return result;
    }

    // ================================================================ vérification d'une affectation

    /**
     * Vérifie qu'on peut affecter une intervention (catégorie skillName, durée en heures) à ce technicien
     * ce jour-là. Lève une ConflictException qui nomme la règle violée, sinon ne fait rien.
     * À appeler par le module 4 avant d'enregistrer une affectation manuelle.
     */
    public void checkCanAssign(int technicianId, String skillName, LocalDate date, BigDecimal duration)
            throws SQLException {
        Technician t = getTechnician(technicianId);

        if (!t.hasSkill(skillName)) {
            throw new ConflictException(Rule.COMPETENCE_MANQUANTE,
                    "Compétence manquante : " + t.getFirstName() + " n'a pas la compétence "
                  + skillName + " requise par cette demande.");
        }
        if (!isAvailable(technicianId, date)) {
            throw new ConflictException(Rule.TECHNICIEN_INDISPONIBLE,
                    "Technicien indisponible : " + t.getFirstName() + " est déclaré(e) indisponible le " + date + ".");
        }
        BigDecimal load = getDailyLoad(technicianId, date);
        BigDecimal total = load.add(duration);
        if (total.compareTo(t.getDailyCapacity()) > 0) {
            throw new ConflictException(Rule.CAPACITE_DEPASSEE,
                    "Capacité dépassée : " + t.getFirstName() + " a déjà " + h(load) + " le " + date
                  + " (" + h(load) + " + " + h(duration) + " = " + h(total)
                  + " pour " + h(t.getDailyCapacity()) + " disponibles).");
        }
    }

    // ================================================================ pour l'algorithme (membre 5)

    /**
     * Techniciens éligibles pour une intervention : compétents, disponibles ce jour,
     * avec une capacité restante suffisante. Triés par charge croissante puis prénom
     * (règle de départage du glouton dans le sujet).
     *
     * @param pendingLoad heures déjà proposées pendant la planification en cours mais pas encore
     *                    validées en base (technicienId -> heures). Peut être null.
     */
    public List<Technician> findEligible(String skillName, LocalDate date, BigDecimal duration,
                                         Map<Integer, BigDecimal> pendingLoad) throws SQLException {
        Map<Integer, BigDecimal> pending = pendingLoad == null ? Map.of() : pendingLoad;
        Map<Integer, BigDecimal> loads = new HashMap<>();
        List<Technician> eligible = new ArrayList<>();

        for (Technician t : repo.findAll()) {
            if (!t.hasSkill(skillName) || !isAvailable(t.getId(), date)) continue;
            BigDecimal load = getDailyLoad(t.getId(), date)
                    .add(pending.getOrDefault(t.getId(), BigDecimal.ZERO));
            if (load.add(duration).compareTo(t.getDailyCapacity()) <= 0) {
                loads.put(t.getId(), load);
                eligible.add(t);
            }
        }
        eligible.sort(Comparator
                .comparing((Technician t) -> loads.get(t.getId()))
                .thenComparing(Technician::getFirstName, String.CASE_INSENSITIVE_ORDER));
        return eligible;
    }

    public List<Technician> findEligible(String skillName, LocalDate date, BigDecimal duration)
            throws SQLException {
        return findEligible(skillName, date, duration, null);
    }

    // ================================================================ helpers

    private void validateCapacity(BigDecimal capacity) {
        if (capacity.signum() <= 0 || capacity.compareTo(MAX_CAPACITY) > 0) {
            throw new ConflictException(Rule.DONNEE_INVALIDE,
                    "La capacité journalière doit être comprise entre 0 et 8 heures.");
        }
    }

    /** 6.00 -> "6 h" */
    static String h(BigDecimal hours) {
        return hours.stripTrailingZeros().toPlainString() + " h";
    }

    /** Ligne de l'écran de charge : technicien, jour, charge, capacité, disponibilité. */
    public static class DailyLoad {
        public final Technician technician;
        public final LocalDate date;
        public final BigDecimal load;
        public final BigDecimal capacity;
        public final boolean available;

        public DailyLoad(Technician technician, LocalDate date, BigDecimal load,
                         BigDecimal capacity, boolean available) {
            this.technician = technician;
            this.date = date;
            this.load = load;
            this.capacity = capacity;
            this.available = available;
        }

        public BigDecimal getRemaining() {
            return available ? capacity.subtract(load).max(BigDecimal.ZERO) : BigDecimal.ZERO;
        }

        @Override
        public String toString() {
            String names = technician.getSkills().stream().map(Skill::getName).collect(Collectors.joining(", "));
            return technician.getFirstName() + " [" + names + "] : "
                 + (available ? h(load) + " / " + h(capacity) : "indisponible");
        }
    }
}
