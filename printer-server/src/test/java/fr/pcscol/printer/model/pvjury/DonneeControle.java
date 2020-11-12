package fr.pcscol.printer.model.pvjury;

public class DonneeControle {

    private String resultat;
    private float note;

    public String getResultat() {
        return resultat;
    }

    public DonneeControle setResultat(String resultat) {
        this.resultat = resultat;
        return this;
    }

    public float getNote() {
        return note;
    }

    public DonneeControle setNote(float note) {
        this.note = note;
        return this;
    }

}
