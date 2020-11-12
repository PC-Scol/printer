package fr.pcscol.printer.model.pvjury;

public class DonneeEvaluationFinale {

    private String resultat;
    private int bareme;
    private float creditEcts;
    private String mention;
    private float gradeGpa;
    private String notationEcts;
    private float noteFinale;
    private float pointJury;
    private float noteObjet;

    public DonneeEvaluationFinale(){}

    public String getResultat() {
        return resultat;
    }

    public DonneeEvaluationFinale setResultat(String resultat) {
        this.resultat = resultat;
        return this;
    }

    public int getBareme() {
        return bareme;
    }

    public DonneeEvaluationFinale setBareme(int bareme) {
        this.bareme = bareme;
        return this;
    }

    public float getCreditEcts() {
        return creditEcts;
    }

    public DonneeEvaluationFinale setCreditEcts(float creditEcts) {
        this.creditEcts = creditEcts;
        return this;
    }

    public String getMention() {
        return mention;
    }

    public DonneeEvaluationFinale setMention(String mention) {
        this.mention = mention;
        return this;
    }

    public float getGradeGpa() {
        return gradeGpa;
    }

    public DonneeEvaluationFinale setGradeGpa(float gradeGpa) {
        this.gradeGpa = gradeGpa;
        return this;
    }

    public String getNotationEcts() {
        return notationEcts;
    }

    public DonneeEvaluationFinale setNotationEcts(String notationEcts) {
        this.notationEcts = notationEcts;
        return this;
    }

    public float getNoteFinale() {
        return noteFinale;
    }

    public DonneeEvaluationFinale setNoteFinale(float noteFinale) {
        this.noteFinale = noteFinale;
        return this;
    }

    public float getPointJury() {
        return pointJury;
    }

    public DonneeEvaluationFinale setPointJury(float pointJury) {
        this.pointJury = pointJury;
        return this;
    }

    public float getNoteObjet() {
        return noteObjet;
    }

    public DonneeEvaluationFinale setNoteObjet(float noteObjet) {
        this.noteObjet = noteObjet;
        return this;
    }

}
