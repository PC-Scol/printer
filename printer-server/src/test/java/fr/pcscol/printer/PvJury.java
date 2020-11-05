package fr.pcscol.printer;

import java.util.List;

public class PvJury {

    public static class ObjetFormationDesc {
        private String groupement;
        private String libelle;

        public ObjetFormationDesc(String groupement, String libelle) {
            this.groupement = groupement;
            this.libelle = libelle;
        }

        public String getGroupement() {
            return groupement;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public static class Apprenant {

        public static class ObjetFormation {
            private int bareme;
            private int credit;
            private String resultat;
            private float note;

            public ObjetFormation setBareme(int bareme) {
                this.bareme = bareme;
                return this;
            }

            public ObjetFormation setCredit(int credit) {
                this.credit = credit;
                return this;
            }

            public ObjetFormation setResultat(String resultat) {
                this.resultat = resultat;
                return this;
            }

            public ObjetFormation setNote(float note) {
                this.note = note;
                return this;
            }

            public int getBareme() {
                return bareme;
            }

            public int getCredit() {
                return credit;
            }

            public String getResultat() {
                return resultat;
            }

            public float getNote() {
                return note;
            }

            public ObjetFormation() {

            }

        }

        private String nom;
        private String prenom;
        private String numero;
        private float noteFormation;
        private List<ObjetFormation> objetFormations;

        public Apprenant(String nom, String prenom, String numero) {
            this.nom = nom;
            this.prenom = prenom;
            this.numero = numero;
        }

        public Apprenant setNoteFormation(float noteFormation) {
            this.noteFormation = noteFormation;
            return this;
        }

        public Apprenant setObjetFormations(List<ObjetFormation> objetFormations) {
            this.objetFormations = objetFormations;
            return this;
        }

        public String getNom() {
            return nom;
        }

        public String getPrenom() {
            return prenom;
        }

        public String getNumero() {
            return numero;
        }

        public float getNoteFormation() {
            return noteFormation;
        }

        public List<ObjetFormation> getObjetFormations() {
            return objetFormations;
        }
    }

    private String titre;
    private String periode;
    private String libelleFormation;
    private List<ObjetFormationDesc> objetFormationDescs;
    private List<Apprenant> apprenants;

    public PvJury(String titre, String periode, String libelleFormation) {
        this.titre = titre;
        this.periode = periode;
        this.libelleFormation = libelleFormation;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getLibelleFormation() {
        return libelleFormation;
    }

    public void setLibelleFormation(String libelleFormation) {
        this.libelleFormation = libelleFormation;
    }

    public List<ObjetFormationDesc> getObjetFormationDescs() {
        return objetFormationDescs;
    }

    public void setObjetFormationDescs(List<ObjetFormationDesc> objetFormationDescs) {
        this.objetFormationDescs = objetFormationDescs;
    }

    public List<Apprenant> getApprenants() {
        return apprenants;
    }

    public void setApprenants(List<Apprenant> apprenants) {
        this.apprenants = apprenants;
    }
}
