package fr.pcscol.printer.model.pvjury;

public class Controle {

    private boolean resultat;
    private boolean note;

    public Controle() {
    }

    public boolean isResultat() {
        return resultat;
    }

    public Controle setResultat(boolean resultat) {
        this.resultat = resultat;
        return this;
    }

    public boolean isNote() {
        return note;
    }

    public Controle setNote(boolean note) {
        this.note = note;
        return this;
    }
}
