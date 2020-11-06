package fr.pcscol.printer.service.freemarker;

import com.google.common.collect.Lists;
import fr.pcscol.printer.PvJury;
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

/**
 * Tests the {@link FreemarkerPrinterService} layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FreemarkerPrinterServiceTest {

    static {
        TomcatURLStreamHandlerFactory.getInstance();
    }

    @Autowired
    private FreemarkerPrinterService freemarkerPrinterService;

    @Test
    public void generateTest() throws IOException {

        File outFile = File.createTempFile("pv_out_", ".csv", new File("build"));
        outFile.deleteOnExit();

        //data
        PvJury pvJury = new PvJury("PV de jury provisoire", "Période : Année universitaire 2019/2020", "ING-ENIT-FI1-S1 Semestre 1");
        pvJury.setObjetFormationDescs(Lists.newArrayList(
                new PvJury.ObjetFormationDesc("GRP-1 Choix UE", "ING-ENIT-FI-UE-01 UE01-Mathématiques appliquées"),
                new PvJury.ObjetFormationDesc("GRP-1 Choix UE", "ING-ENIT-FI-UE-02 UE02-Physique des matériaux"),
                new PvJury.ObjetFormationDesc("GRP-2 Choix UE", "ING-ENIT-FI-UE-03 UE03-Informatique industrielle")
        ));

        pvJury.setApprenants(Lists.newArrayList(
                new PvJury.Apprenant("Dupont", "Jean", "111").setNoteFormation(11f).setObjetFormations(Lists.newArrayList(
                        new PvJury.Apprenant.ObjetFormation().setBareme(20).setCredit(5).setNote(11f).setResultat(null),
                        new PvJury.Apprenant.ObjetFormation().setBareme(20).setCredit(4).setNote(11f).setResultat("ADM"),
                        new PvJury.Apprenant.ObjetFormation().setBareme(20).setCredit(4).setNote(1f).setResultat("RECALE")
                )),
                new PvJury.Apprenant("Dumont", "Pierre", "222").setNoteFormation(14f).setObjetFormations(Lists.newArrayList(
                        new PvJury.Apprenant.ObjetFormation().setBareme(20).setCredit(5).setNote(12f).setResultat(null),
                        new PvJury.Apprenant.ObjetFormation().setBareme(20).setCredit(4).setNote(12f).setResultat("ADM"),
                        new PvJury.Apprenant.ObjetFormation().setBareme(20).setCredit(4).setNote(2f).setResultat("RECALE")
                )),
                new PvJury.Apprenant("Duchamps", "Didier", "333").setNoteFormation(13f).setObjetFormations(Lists.newArrayList(
                        null,
                        new PvJury.Apprenant.ObjetFormation().setBareme(20).setCredit(4).setNote(13f).setResultat("ADM"),
                        null
                ))
        ));

        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            freemarkerPrinterService.generate("pv.csv", pvJury, outputStream);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
