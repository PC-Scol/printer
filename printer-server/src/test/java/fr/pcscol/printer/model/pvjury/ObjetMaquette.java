package fr.pcscol.printer.model.pvjury;

public class ObjetMaquette {

    private final String groupement;
    private final String code;
    private final String libelle;
    private EvaluationFinale evaluationFinale;
    private Session session1;
    private Session session2;

    public ObjetMaquette(String groupement, String code, String libelle) {
        this.groupement = groupement;
        this.code = code;
        this.libelle = libelle;
    }

    public String getGroupement() {
        return groupement;
    }

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    public EvaluationFinale getEvaluationFinale() {
        return evaluationFinale;
    }

    public ObjetMaquette setEvaluationFinale(EvaluationFinale evaluationFinale) {
        this.evaluationFinale = evaluationFinale;
        return this;
    }

    public Session getSession1() {
        return session1;
    }

    public ObjetMaquette setSession1(Session session1) {
        this.session1 = session1;
        return this;
    }

    public Session getSession2() {
        return session2;
    }

    public ObjetMaquette setSession2(Session session2) {
        this.session2 = session2;
        return this;
    }
}
