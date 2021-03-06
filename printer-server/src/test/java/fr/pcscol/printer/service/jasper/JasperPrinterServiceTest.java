package fr.pcscol.printer.service.jasper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the {@link JasperPrinterService} layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class JasperPrinterServiceTest {

    static {
        TomcatURLStreamHandlerFactory.getInstance();
    }

    @Autowired
    private JasperPrinterService printerService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests a correct generation of a PDF document from a Jasper (with nested sub report) template and data.
     *
     * @throws IOException
     */
    @Test
    public void generatePdfWithNestedReportTest() throws IOException {

        //templateUrl
        URL templateUrl = this.getClass().getResource("/jasper/releveNote.zip");

        File outFile = File.createTempFile("releve_multiple_out_", ".pdf", new File("build"));
        outFile.deleteOnExit();

        //json data input
        List<Object> json = objectMapper.readValue(this.getClass().getResourceAsStream("/jasper/releveNote/releveNoteMultiple.json"), ArrayList.class);
        JsonNode data = objectMapper.convertValue(json, JsonNode.class);
        Map<String, Object> params = new HashMap<>();
        params.put("fr.pcscol.logo", "logo2.png");
        params.put("fr.pcscol.nomResponsable", "Jean Bernard");
        params.put("fr.pcscol.nomEtablissement", "INU Champollion");
        params.put(JasperExporterConfigParams.ALL_EXPORT_METADATA_TITLE.toString(), "title of the pdf");
        params.put(JasperExporterConfigParams.ALL_EXPORT_METADATA_SUBJECT.toString(), "subject");
        params.put(JasperExporterConfigParams.ALL_EXPORT_METADATA_AUTHOR.toString(), "pegase");
        params.put(JasperExporterConfigParams.PDF_EXPORT_METADATA_DISPLAY_TITLE.toString(), true);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, data, params, JasperExportType.PDF, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void generateOdtWithNestedReportTest() throws IOException {

        //templateUrl
        URL templateUrl = this.getClass().getResource("/jasper/releveNote.zip");

        File outFile = File.createTempFile("releve_multiple_out_", ".odt", new File("build"));
        outFile.deleteOnExit();

        //json data input
        List<Object> json = objectMapper.readValue(this.getClass().getResourceAsStream("/jasper/releveNote/releveNoteMultiple.json"), ArrayList.class);
        JsonNode data = objectMapper.convertValue(json, JsonNode.class);
        Map<String, Object> params = new HashMap<>();
        params.put("fr.pcscol.logo", "logo.gif");
        params.put("fr.pcscol.nomResponsable", "Jean Bernard");
        params.put("fr.pcscol.nomEtablissement", "INU Champollion");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, data, null, JasperExportType.ODT, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void generatCsvWithNestedReportTest() throws IOException {

        //templateUrl
        URL templateUrl = this.getClass().getResource("/jasper/releveNote.zip");

        File outFile = File.createTempFile("releve_multiple_out_", ".csv", new File("build"));
        outFile.deleteOnExit();

        //json data input
        List<Object> json = objectMapper.readValue(this.getClass().getResourceAsStream("/jasper/releveNote/releveNoteMultiple.json"), ArrayList.class);
        JsonNode data = objectMapper.convertValue(json, JsonNode.class);
        Map<String, Object> params = new HashMap<>();
        params.put("fr.pcscol.logo", "logo.gif");
        params.put("fr.pcscol.nomResponsable", "Jean Bernard");
        params.put("fr.pcscol.nomEtablissement", "INU Champollion");
        params.put(JasperExporterConfigParams.CSV_EXPORT_RECORD_DELIMITER.toString(), "\n");
        params.put(JasperExporterConfigParams.CSV_EXPORT_FIELD_DELIMITER.toString(), ";");
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, data, null, JasperExportType.CSV, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void generateWithTemplateNotFoundTest() throws IOException {

        //templateUrl
        URL templateUrl = new URL("file://path/to/notfound.zip");

        //json data input
        List<Object> json = objectMapper.readValue(this.getClass().getResourceAsStream("/jasper/releveNote/releveNoteMultiple.json"), ArrayList.class);
        JsonNode data = objectMapper.convertValue(json, JsonNode.class);
        try {
            printerService.generate(templateUrl, data, null, JasperExportType.PDF, null);
            Assert.fail("Should fail");
        } catch (TemplateNotFoundException e) {
            //success
        } catch (Exception e) {
            Assert.fail("Should throw TemplateNotFoundException");
        }
    }
}
