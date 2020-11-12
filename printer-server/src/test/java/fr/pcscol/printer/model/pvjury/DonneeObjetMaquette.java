package fr.pcscol.printer.model.pvjury;

public class DonneeObjetMaquette {

    private String amenagement;
    private DonneeEvaluationFinale evaluationFinale;
    private DonneeSession session1;
    private DonneeSession session2;

    public String getAmenagement() {
        return amenagement;
    }

    public DonneeObjetMaquette setAmenagement(String amenagement) {
        this.amenagement = amenagement;
        return this;
    }

    public DonneeEvaluationFinale getEvaluationFinale() {
        return evaluationFinale;
    }

    public DonneeObjetMaquette setEvaluationFinale(DonneeEvaluationFinale evaluationFinale) {
        this.evaluationFinale = evaluationFinale;
        return this;
    }

    public DonneeSession getSession1() {
        return session1;
    }

    public DonneeObjetMaquette setSession1(DonneeSession session1) {
        this.session1 = session1;
        return this;
    }

    public DonneeSession getSession2() {
        return session2;
    }

    public DonneeObjetMaquette setSession2(DonneeSession session2) {
        this.session2 = session2;
        return this;
    }
}
