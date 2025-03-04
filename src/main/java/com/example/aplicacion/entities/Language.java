package com.example.aplicacion.entities;

import com.example.aplicacion.pojos.LanguageAPI;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    private String nombreLenguaje;

    private String imgenId;
    private long timestamp = Instant.now().toEpochMilli();

    @ManyToMany
    private Set<Contest> concursos;

    public Language() {
        concursos = new HashSet<>();
    }

    public Language(String lenguaje, String imgenId) {
        this.nombreLenguaje = lenguaje;
        this.imgenId = imgenId;
    }

    public LanguageAPI toLanguageAPI() {
        LanguageAPI languageAPI = toLanguageAPISimple();
        languageAPI.setTimestamp(this.timestamp);
        return languageAPI;
    }

    public LanguageAPI toLanguageAPISimple() {
        LanguageAPI languageAPI = new LanguageAPI();
        languageAPI.setId(this.id);
        languageAPI.setNombreLenguaje(nombreLenguaje);
        return languageAPI;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombreLenguaje() {
        return nombreLenguaje;
    }

    public void setNombreLenguaje(String nombreLenguaje) {
        this.nombreLenguaje = nombreLenguaje;
    }

    public String getImgenId() {
        return imgenId;
    }

    public void setImgenId(String imgenId) {
        this.imgenId = imgenId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Set<Contest> getConcursos() {
        return concursos;
    }

    public void setConcursos(Set<Contest> concursos) {
        this.concursos = concursos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Language language = (Language) o;
        return id == language.getId() && nombreLenguaje.equals(language.getNombreLenguaje());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombreLenguaje);
    }
}
