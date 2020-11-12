package fr.pcscol.printer.model.pvjury;

import java.util.List;

public class PvJury {

    private String libellePvJury;
    private String libellePeriode;
    private List<ObjetMaquette> listeObjetMaquette;
    private List<Apprenant> listeApprenants;

    public PvJury(){}

    public PvJury(String libellePvJury, String libellePeriode) {
        this.libellePvJury = libellePvJury;
        this.libellePeriode = libellePeriode;
    }

    public String getLibellePvJury() {
        return libellePvJury;
    }

    public void setLibellePvJury(String libellePvJury) {
        this.libellePvJury = libellePvJury;
    }

    public String getLibellePeriode() {
        return libellePeriode;
    }

    public void setLibellePeriode(String libellePeriode) {
        this.libellePeriode = libellePeriode;
    }

    public List<ObjetMaquette> getListeObjetMaquette() {
        return listeObjetMaquette;
    }

    public PvJury setListeObjetMaquette(List<ObjetMaquette> listeObjetMaquette) {
        this.listeObjetMaquette = listeObjetMaquette;
        return this;
    }

    public List<Apprenant> getListeApprenants() {
        return listeApprenants;
    }

    public PvJury setListeApprenants(List<Apprenant> listeApprenants) {
        this.listeApprenants = listeApprenants;
        return this;
    }
}
