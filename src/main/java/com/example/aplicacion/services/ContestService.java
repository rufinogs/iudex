package com.example.aplicacion.services;

import com.example.aplicacion.Entities.Contest;
import com.example.aplicacion.Entities.Language;
import com.example.aplicacion.Entities.Problem;
import com.example.aplicacion.Entities.Team;
import com.example.aplicacion.Pojos.ContestString;
import com.example.aplicacion.Pojos.ProblemAPI;
import com.example.aplicacion.Repository.ContestRepository;
import com.example.aplicacion.Repository.ProblemRepository;
import com.example.aplicacion.Repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContestService {
    Logger logger = LoggerFactory.getLogger(ContestService.class);
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private TeamService teamService;

    public ContestString creaContest(String nameContest, String teamId, Optional<String> descripcion) {
        logger.debug("Build contest " + nameContest + "\nTeam: " + teamId + "\nDescription: " + descripcion);
        ContestString salida = new ContestString();
        if (contestRepository.existsByNombreContest(nameContest)) {
            logger.error("Contest name duplicated " + nameContest);
            salida.setSalida("contest NAME DUPLICATED");
            return salida;
        }

        Contest contest = new Contest();
        contest.setNombreContest(nameContest);

        if (descripcion.isPresent()) {
            contest.setDescripcion(descripcion.get());
        }

        Optional<Team> teamOptional = teamService.getTeamFromId(teamId);
        if (teamOptional.isEmpty()) {
            logger.error("Team " + teamId + " not found");
            salida.setSalida("TEAM NOT FOUND");
            return salida;
        }
        Team team = teamOptional.get();
        contest.setTeamPropietario(team);

        Language java = languageService.getLanguageByName("java").get();
        contest.addLanguage(java);
        contestRepository.save(contest);

        salida.setSalida("OK");
        salida.setContest(contest);

        logger.debug("Finish build contest " + nameContest + "\nId: " + contest.getId());
        return salida;
    }

    public ContestString updateContest(String contestId, Optional<String> nameContest, Optional<String> teamId, Optional<String> descripcion) {
        logger.debug("Update contest " + contestId);
        ContestString salida = new ContestString();

        //Buscamos el contest
        Optional<Contest> contestOptional = getContest(contestId);
        if (contestOptional.isEmpty()) {
            logger.error("Contest " + contestId + " not found");
            salida.setSalida("CONTEST ID DOES NOT EXIST");
            return salida;
        }
        Contest contest = contestOptional.get();

        //Si namecontest esta presente lo cambiamos
        if (nameContest.isPresent()) {
            if (contestRepository.existsByNombreContest(nameContest.get())) {
                logger.error("Contest name " + nameContest.get() + " duplicated");
                salida.setSalida("CONTEST NAME DUPLICATED");
                return salida;
            }
            String contestName = nameContest.get();
            contest.setNombreContest(contestName);
        }

        if (teamId.isPresent()) {
            Optional<Team> teamOptional = teamService.getTeamFromId(teamId.get());
            if (teamOptional.isEmpty()) {
                logger.error("Team " + teamId.get() + " not found");
                salida.setSalida("TEAM NOT FOUND");
                return salida;
            }
            Team team = teamOptional.get();
            contest.setTeamPropietario(team);
        }
        if (descripcion.isPresent()) {
            contest.setDescripcion(descripcion.get());
        }
        contestRepository.save(contest);

        salida.setSalida("OK");
        salida.setContest(contest);

        logger.debug("Finish update contest " + contestId);
        return salida;
    }


    public String deleteContest(String idcontest) {
        logger.debug("Delete contest " + idcontest);

        Optional<Contest> optionalContest = getContest(idcontest);
        if (optionalContest.isEmpty()) {
            logger.error("Contest " + idcontest + " not found");
            return "contest NOT FOUND";
        }
        Contest contest = optionalContest.get();

        // quitar contest de los participantes
        for (Team teamAux : contest.getListaParticipantes()) {
            teamAux.getListaContestsParticipados().remove(contest);
        }

        //borramos el contest
        contestRepository.delete(contest);

        logger.debug("Finish delete contest " + idcontest);
        return "OK";
    }

    public String anyadeProblemaContest(String idContest, String idProblema) {
        logger.debug("Add problem " + idProblema + " to contest " + idContest);
        Optional<Contest> contestOptional = getContest(idContest);
        Optional<Problem> problemOptional = problemService.getProblem(idProblema);

        if (contestOptional.isEmpty()) {
            logger.error("Contest " + idContest + " not found");
            return "contest NOT FOUND";
        }
        Contest contest = contestOptional.get();

        if (problemOptional.isEmpty()) {
            logger.error("Problem " + idProblema + " not found");
            return "PROBLEM NOT FOUND";
        }
        Problem problema = problemOptional.get();

        if (contest.getListaProblemas().contains(problema)) {
            logger.error("Problem " + idProblema + " already in contest");
            return "PROBLEM DUPLICATED";
        }

        contest.addProblem(problema);
        contestRepository.save(contest);

        logger.debug("Finish add problem " + idProblema + " to contest " + idContest);
        return "OK";
    }

    public String deleteProblemFromContest(String idContest, String idProblema) {
        logger.debug("Delete problem " + idProblema + " from contest " + idContest);
        Optional<Contest> contestOptional = getContest(idContest);
        Optional<Problem> problemaOptional = problemService.getProblem(idProblema);

        if (contestOptional.isEmpty()) {
            logger.error("Contest " + idContest + " not found");
            return "contest NOT FOUND";
        }
        Contest contest = contestOptional.get();

        if (problemaOptional.isEmpty()) {
            logger.error("Problem " + idProblema + " not found");
            return "PROBLEM NOT FOUND";
        }
        Problem problema = problemaOptional.get();

        if (!contest.getListaProblemas().contains(problema)) {
            logger.error("Problem " + idProblema + " not in contest " + idContest);
            return "PROBLEM NOT IN CONCURSO";
        }

        contest.deleteProblem(problema);
        contestRepository.save(contest);

        logger.debug("Finish delete problem " + idProblema + " from contest " + idContest);
        return "OK";
    }

    public String addTeamTocontest(String idTeam, String idcontest) {
        logger.debug("Add team/user " + idTeam + " to contest " + idcontest);

        Optional<Contest> contestOptional = getContest(idcontest);
        if (contestOptional.isEmpty()) {
            logger.error("Contest " + idcontest + " not found");
            return "contest NOT FOUND";
        }
        Contest contest = contestOptional.get();

        Optional<Team> teamOptional = teamService.getTeamFromId(idTeam);
        if (teamOptional.isEmpty()) {
            logger.error("Team/user " + idTeam + " not found");
            return "USER NOT FOUND";
        }
        Team team = teamOptional.get();

        if (!contest.getListaParticipantes().contains(team)) {
            contest.addTeam(team);
            contestRepository.save(contest);
        } else {
            logger.error("Team/user " + idTeam + " already in contest " + idcontest);
            return "YA ESTA EN EL CONCURSO";
        }
        logger.debug("Finish add team/user " + idTeam + " to contest " + idcontest);
        return "OK";
    }

    public String deleteTeamFromcontest(String idcontest, String idTeam) {
        logger.debug("Delete team/user " + idTeam + " from contest " + idcontest);

        Optional<Contest> contestOptional = getContest(idcontest);
        if (contestOptional.isEmpty()) {
            logger.error("Contest " + idcontest + " not found");
            return "contest NOT FOUND";
        }
        Contest contest = contestOptional.get();

        Optional<Team> teamOptional = teamService.getTeamFromId(idTeam);
        if (teamOptional.isEmpty()) {
            logger.error("Team/user " + idTeam + " not found");
            return "USER NOT FOUND";
        }
        Team team = teamOptional.get();

        if (!contest.getListaParticipantes().contains(team)) {
            logger.error("Team/user " + idTeam + " not in contest " + idcontest);
            return "NO ESTA EN EL CONCURSO";
        } else {
            contest.deleteTeam(team);
            contestRepository.save(contest);
        }
        logger.debug("Finish delete team/user " + idTeam + " from contest " + idcontest);
        return "OK";
    }

    public List<ProblemAPI> getProblemsFromConcurso(Contest contest) {
        return contest.getListaProblemas().stream().map(x -> x.toProblemAPI()).collect(Collectors.toList());
    }

    public Optional<Contest> getContest(String idContest) {
        return contestRepository.findContestById(Long.parseLong(idContest));
    }

    public List<Contest> getAllContests() {
        return contestRepository.findAll();
    }

    public Page<Contest> getContestPage(Pageable pageable) {
        return contestRepository.findAll(pageable);
    }

    public String addLanguageToContest(String contestId, String languageName) {
        Optional<Contest> contestOptional = getContest(contestId);
        if (contestOptional.isEmpty()) {
            logger.error("Contest " + contestId + " not found!");
            return "CONTEST NOT FOUND!";
        }
        Contest contest = contestOptional.get();
        return addLanguageToContest(contest, languageName);
    }

    public String removeLanguageFromContest(String contestId, String languageId) {
        logger.debug("Removing language " + languageId + " from contest " + contestId);
        String salida;

        Optional<Contest> contestOptional = getContest(contestId);
        if (contestOptional.isEmpty()) {
            logger.error("Contest " + contestId + " not found!");
            salida = "CONTEST NOT FOUND!";
            return salida;
        }
        Contest contest = contestOptional.get();

        Optional<Language> languageOptional = languageService.getLanguage(languageId);
        if (languageOptional.isEmpty()) {
            logger.error("Language " + languageId + " not found!");
            salida = "LANGUAGE NOT FOUND!";
            return salida;
        }
        Language language = languageOptional.get();

        contest.removeLanguage(language);
        contestRepository.save(contest);

        logger.debug("Finish delete language " + languageId + " from contest " + contestId);
        salida = "OK";
        return salida;
    }

    public String addAcceptedLanguagesToContest(String contestId, String[] languageList) {
        logger.debug("Add accepted languages to contest " + contestId);
        String salida;

        Optional<Contest> contestOptional = getContest(contestId);
        if (contestOptional.isEmpty()) {
            logger.error("Contest " + contestId + " not found!");
            salida = "CONTEST NOT FOUND!";
            return salida;
        }
        Contest contest = contestOptional.get();

        for (String languageName : languageList) {
            salida = addLanguageToContest(contest, languageName);

            if (!salida.equals("OK")) {
                logger.debug("Clearing language list from contest " + contestId);
                contest.clearLanguage();
                logger.debug("Adding default language");
                addLanguageToContest(contest, "java");
                contestRepository.save(contest);
                return salida;
            }
        }

        logger.debug("Finish add accepted languages to contest " + contestId);
        salida = "OK";
        return salida;
    }

    private String addLanguageToContest(Contest contest, String languageName) {
        logger.debug("Adding language " + languageName + " to contest " + contest.getId());
        String salida;
        languageName = languageName.trim().toLowerCase();

        Optional<Language> languageOptional = languageService.getLanguageByName(languageName);
        if (languageOptional.isEmpty()) {
            logger.error("Unknown language " + languageName);
            salida = "UNKNOWN LANGUAGE " + languageName.toUpperCase() + "!";
            return salida;
        }
        Language language = languageOptional.get();

        // add language
        contest.addLanguage(language);
        contestRepository.save(contest);

        logger.debug("Finish add language " + languageName + " to contest " + contest.getId());
        salida = "OK";
        return salida;
    }
}
