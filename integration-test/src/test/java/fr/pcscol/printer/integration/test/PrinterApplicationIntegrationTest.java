package fr.pcscol.printer.integration.test;

import fr.pcscol.printer.adapter.PrinterException;
import fr.pcscol.printer.client.api.PrinterApi;
import fr.pcscol.printer.client.api.model.PrintMessage;
import fr.pcscol.printer.client.v2.api.model.FreemarkerPrintMessage;
import fr.pcscol.printer.client.v2.api.model.JasperExporterConfigParams;
import fr.pcscol.printer.client.v2.api.model.JasperPrintMessage;
import fr.pcscol.printer.client.v2.api.model.XdocPrintMessage;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Tests the client/server integration
 */
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = PrinterApplicationTestConfiguration.class)
public class PrinterApplicationIntegrationTest {

    @Autowired
    private PrinterApi printerApiV1;

    @Autowired
    private fr.pcscol.printer.client.v2.api.PrinterApi printerApiV2;

    private Boolean keepFilesEnv = Boolean.valueOf(System.getenv("keepFiles"));

    /**
     * Tests the client/server integration by invoking the real WS.
     *
     * @throws IOException
     */
    @Test
    public void printV1_OKTest() throws IOException {

        File outFile = File.createTempFile("PrinterApplicationIntegrationTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        //build PrintMessage
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        PrintMessage printMessage = new PrintMessage().templateUrl("certificat.odt").data(personBean).convert(true);
        printerApiV1.getApiClient().setBasePath("http://localhost:8080/printer/v1");
        byte[] content = printerApiV1.print(printMessage);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            outputStream.write(content);
        }
        Assert.assertThat(content.length, Matchers.greaterThan(0));

    }

    @Test
    public void printV1_Error404Test() throws Exception {

        File outFile = File.createTempFile("PrinterApplicationIntegrationTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        //build PrintMessage
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        PrintMessage printMessage = new PrintMessage().templateUrl("template_not_allowed.txt").data(personBean).convert(true);

        try {
            printerApiV1.print(printMessage);
            Assert.fail();
        } catch (PrinterException e) {
            Assert.assertThat(e.getMessage(), Matchers.containsString("404"));
        }

    }

    @Test
    public void jasperPrintV2_OKTest() throws IOException {

        File outFile = File.createTempFile("PrinterV2ApplicationIntegrationTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
           //outFile.deleteOnExit();
        }

        //build PrintMessage
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> params = Map.of(
                JasperExporterConfigParams.PDF_EXPORT_DISPLAYMETADATATITLE.getValue(), true,
                JasperExporterConfigParams.ALL_EXPORT_METADATATITLE.getValue(), "Mon Certificat");
        JasperPrintMessage printMessage = new JasperPrintMessage().templateName("certificat").data(personBean).parameters(params);
        printerApiV2.getApiClient().setBasePath("http://localhost:8080/printer/v2");
        byte[] content = printerApiV2.jasperPrint(printMessage);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            outputStream.write(content);
        }
        Assert.assertThat(content.length, Matchers.greaterThan(0));

    }

    @Test
    public void xdocPrintV2_OKTest() throws IOException {

        File outFile = File.createTempFile("PrinterV2ApplicationIntegrationTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        //build PrintMessage
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        XdocPrintMessage printMessage = new XdocPrintMessage().templateUrl("certificat.odt").data(personBean).convert(true);
        printerApiV2.getApiClient().setBasePath("http://localhost:8080/printer/v2");
        byte[] content = printerApiV2.xdocPrint(printMessage);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            outputStream.write(content);
        }
        Assert.assertThat(content.length, Matchers.greaterThan(0));

    }

    @Test
    public void freemarkerPrintV2_OKTest() throws IOException {

        File outFile = File.createTempFile("PrinterV2ApplicationIntegrationTest_out_", ".csv", new File("build"));
        if (!keepFilesEnv) {
          //  outFile.deleteOnExit();
        }

        //build PrintMessage
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        FreemarkerPrintMessage printMessage = new FreemarkerPrintMessage().templateName("certificat.csv").data(personBean);
        printerApiV2.getApiClient().setBasePath("http://localhost:8080/printer/v2");
        byte[] content = printerApiV2.freemarkerPrint(printMessage);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            outputStream.write(content);
        }
        Assert.assertThat(content.length, Matchers.greaterThan(0));

    }
}
