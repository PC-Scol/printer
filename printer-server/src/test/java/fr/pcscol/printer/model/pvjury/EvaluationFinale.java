package fr.pcscol.printer.model.pvjury;

public class EvaluationFinale {

    private boolean resultat;
    private boolean creditEcts;
    private boolean mention;
    private boolean gradeGpa;
    private boolean notationEcts;
    private boolean noteFinale;
    private boolean pointJury;
    private boolean noteObjet;


    public EvaluationFinale() {
    }

    public boolean isResultat() {
        return resultat;
    }

    public EvaluationFinale setResultat(boolean resultat) {
        this.resultat = resultat;
        return this;
    }

    public boolean isCreditEcts() {
        return creditEcts;
    }

    public EvaluationFinale setCreditEcts(boolean creditEcts) {
        this.creditEcts = creditEcts;
        return this;
    }

    public boolean isMention() {
        return mention;
    }

    public EvaluationFinale setMention(boolean mention) {
        this.mention = mention;
        return this;
    }

    public boolean isGradeGpa() {
        return gradeGpa;
    }

    public EvaluationFinale setGradeGpa(boolean gradeGpa) {
        this.gradeGpa = gradeGpa;
        return this;
    }

    public boolean isNotationEcts() {
        return notationEcts;
    }

    public EvaluationFinale setNotationEcts(boolean notationEcts) {
        this.notationEcts = notationEcts;
        return this;
    }

    public boolean isNoteFinale() {
        return noteFinale;
    }

    public EvaluationFinale setNoteFinale(boolean noteFinale) {
        this.noteFinale = noteFinale;
        return this;
    }

    public boolean isPointJury() {
        return pointJury;
    }

    public EvaluationFinale setPointJury(boolean pointJury) {
        this.pointJury = pointJury;
        return this;
    }

    public boolean isNoteObjet() {
        return noteObjet;
    }

    public EvaluationFinale setNoteObjet(boolean noteObjet) {
        this.noteObjet = noteObjet;
        return this;
    }
}
