package fr.pcscol.printer.service.freemarker;

import com.google.common.collect.Lists;
import fr.pcscol.printer.model.pvjury.*;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Tests the {@link FreemarkerPrinterService} layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FreemarkerPrinterServiceTest {

    public static final String ADMIS = "ADMIS";
    public static final String NON_ADMIS = "NON ADMIS";
    public static final String BIEN = "Bien";

    static {
        TomcatURLStreamHandlerFactory.getInstance();
    }

    @Autowired
    private FreemarkerPrinterService freemarkerPrinterService;

    @Test
    public void generateTest() throws IOException {

        File outFile = File.createTempFile("pv_out_", ".csv", new File("build"));
        // outFile.deleteOnExit();

        //data
        PvJury pvJury = new PvJury("PV de jury provisoire", "Période : Année universitaire 2019/2020");
        pvJury.setListeObjetMaquette(Lists.newArrayList(
                new ObjetMaquette("GRP-1 Choix UE", "ING-ENIT-FI-UE-01", "UE01-Mathématiques appliquées")
                        .setEvaluationFinale(new EvaluationFinale().setResultat(true).setCreditEcts(true).setMention(true).setNoteFinale(true).setNoteObjet(true))
                        .setSession1(new Session().setResultat(true).setControle1(new Controle().setResultat(true)))
                        .setSession2(new Session().setResultat(true).setNote(true).setControle1(new Controle().setResultat(true).setNote(true)).setControle2(new Controle().setResultat(true).setNote(true)))
                ,
                new ObjetMaquette("GRP-1 Choix UE", "ING-ENIT-FI-UE-02", "UE02-Physique des matériaux")
                        .setEvaluationFinale(new EvaluationFinale().setResultat(true).setGradeGpa(true).setNotationEcts(true))
                        .setSession1(new Session().setResultat(true).setNote(true))
                        .setSession2(new Session().setResultat(true).setNote(true).setControle1(new Controle().setResultat(true).setNote(true)).setControle2(new Controle().setResultat(true).setNote(true)))
        ));

        pvJury.setListeApprenants(Lists.newArrayList(
                new Apprenant("111", "Jean", "Dupont").setDonneeObjetMaquetteMap(
                        Map.of("ING-ENIT-FI-UE-01", new DonneeObjetMaquette().setAmenagement("AM1 AM2")
                                        .setEvaluationFinale(new DonneeEvaluationFinale().setResultat(ADMIS).setCreditEcts(2.0f).setMention(BIEN).setNoteFinale(15).setNoteObjet(15.1f))
                                        .setSession1(new DonneeSession().setResultat(ADMIS).setNote(12.564f).setControle1(new DonneeControle().setResultat(ADMIS)))
                                        .setSession2(new DonneeSession().setResultat(ADMIS).setNote(14.564f).setControle1(new DonneeControle().setResultat(NON_ADMIS).setNote(8.88f)).setControle2(new DonneeControle().setResultat(ADMIS).setNote(14.564f)))
                                ,
                                "ING-ENIT-FI-UE-02", new DonneeObjetMaquette().setAmenagement("AM3 AM4")
                                        .setEvaluationFinale(new DonneeEvaluationFinale().setResultat(ADMIS).setGradeGpa(3).setNotationEcts("abc"))
                                        .setSession1(new DonneeSession().setResultat(ADMIS).setNote(16.4f))
                                        .setSession2(new DonneeSession().setResultat(ADMIS).setNote(12.88f).setControle1(new DonneeControle().setResultat(NON_ADMIS).setNote(8.88f)).setControle2(new DonneeControle().setResultat(ADMIS).setNote(12.88f)))
                        )
                )
                ,
                new Apprenant("222", "Marc", "Coco").setDonneeObjetMaquetteMap(
                        Map.of("ING-ENIT-FI-UE-02", new DonneeObjetMaquette().setAmenagement("AM5 AM6")
                                        .setEvaluationFinale(new DonneeEvaluationFinale().setResultat(ADMIS).setGradeGpa(3).setNotationEcts("abc"))
                                        .setSession1(new DonneeSession().setResultat(ADMIS).setNote(18.4f))
                                        .setSession2(new DonneeSession().setResultat(ADMIS).setNote(12.88f).setControle1(new DonneeControle().setResultat(NON_ADMIS).setNote(0.88f)).setControle2(new DonneeControle().setResultat(ADMIS).setNote(19.88f)))
                        )
                )
        ));

        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            // try (OutputStream outputStream = System.out) {
            freemarkerPrinterService.generate("pv_jury.csv", pvJury, outputStream);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
