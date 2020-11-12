package fr.pcscol.printer.model.pvjury;

import java.util.Map;

public class Apprenant {

    private final String code;
    private final String nomFamille;
    private final String prenom;
    private Map<String, DonneeObjetMaquette> donneeObjetMaquetteMap;

    public Apprenant(String code, String nomFamille, String prenom){
        this.code = code;
        this.nomFamille = nomFamille;
        this.prenom = prenom;
    }

    public Apprenant setDonneeObjetMaquetteMap(Map<String, DonneeObjetMaquette> donneeObjetMaquetteMap) {
        this.donneeObjetMaquetteMap = donneeObjetMaquetteMap;
        return this;
    }

    public Map<String, DonneeObjetMaquette> getDonneeObjetMaquetteMap() {
        return donneeObjetMaquetteMap;
    }

    public String getCode() {
        return code;
    }

    public String getNomFamille() {
        return nomFamille;
    }

    public String getPrenom() {
        return prenom;
    }
}
