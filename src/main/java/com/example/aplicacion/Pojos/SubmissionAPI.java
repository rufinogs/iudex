package com.example.aplicacion.Pojos;

import com.example.aplicacion.Entities.Language;
import com.example.aplicacion.Entities.Team;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)

public class SubmissionAPI {

    private long id;
    private List<ResultAPI> results;
    private TeamAPI team;

    private boolean corregido;
    private int numeroResultCorregidos;
    private String resultado;

    private LanguageAPI language;

    private float execSubmissionTime;
    private float execSubmissionMemory;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }



    public List<ResultAPI> getResults() {
        return results;
    }

    public void setResults(List<ResultAPI> results) {
        this.results = results;
    }

    public boolean isCorregido() {
        return corregido;
    }

    public void setCorregido(boolean corregido) {
        this.corregido = corregido;
    }

    public int getNumeroResultCorregidos() {
        return numeroResultCorregidos;
    }

    public void setNumeroResultCorregidos(int numeroResultCorregidos) {
        this.numeroResultCorregidos = numeroResultCorregidos;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public LanguageAPI getLanguage() {
        return language;
    }

    public void setLanguage(LanguageAPI language) {
        this.language = language;
    }

    public float getExecSubmissionTime() {
        return execSubmissionTime;
    }

    public void setExecSubmissionTime(float execSubmissionTime) {
        this.execSubmissionTime = execSubmissionTime;
    }

    public float getExecSubmissionMemory() {
        return execSubmissionMemory;
    }

    public void setExecSubmissionMemory(float execSubmissionMemory) {
        this.execSubmissionMemory = execSubmissionMemory;
    }

    public TeamAPI getTeam() {
        return team;
    }

    public void setTeam(TeamAPI team) {
        this.team = team;
    }
}
