package fr.pcscol.printer.model.pvjury;

public class Session {

    private boolean resultat;
    private boolean note;
    private Controle controle1;
    private Controle controle2;

    public Session() {
    }

    public boolean isResultat() {
        return resultat;
    }

    public Session setResultat(boolean resultat) {
        this.resultat = resultat;
        return this;
    }

    public boolean isNote() {
        return note;
    }

    public Session setNote(boolean note) {
        this.note = note;
        return this;
    }

    public Session setControle1(Controle controle1) {
        this.controle1 = controle1;
        return this;
    }

    public Session setControle2(Controle controle1) {
        this.controle2 = controle1;
        return this;
    }

    public Controle getControle1() {
        return controle1;
    }

    public Controle getControle2() {
        return controle2;
    }
}
