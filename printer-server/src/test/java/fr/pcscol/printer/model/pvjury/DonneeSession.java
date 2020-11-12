package fr.pcscol.printer.model.pvjury;

public class DonneeSession {

    private String resultat;
    private float note;
    private DonneeControle controle1;
    private DonneeControle controle2;

    public String getResultat() {
        return resultat;
    }

    public DonneeSession setResultat(String resultat) {
        this.resultat = resultat;
        return this;
    }

    public float getNote() {
        return note;
    }

    public DonneeSession setNote(float note) {
        this.note = note;
        return this;
    }

    public DonneeControle getControle1() {
        return controle1;
    }

    public DonneeSession setControle1(DonneeControle controle1) {
        this.controle1 = controle1;
        return this;
    }

    public DonneeControle getControle2() {
        return controle2;
    }

    public DonneeSession setControle2(DonneeControle controle2) {
        this.controle2 = controle2;
        return this;
    }
}
