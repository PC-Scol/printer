package fr.pcscol.printer.service.jasper;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
    public void generatePdfWithNestedReportTest() throws IOException, URISyntaxException {

        File outFile = File.createTempFile("releve_multiple_out_", ".pdf", new File("build"));
        //outFile.deleteOnExit();

        //json data input
        List<Object> data = objectMapper.readValue(this.getClass().getResourceAsStream("/jasper/releveNote/releveNoteMultiple.json"), ArrayList.class);
        Map<String, Object> params = null;
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate("releveNote", data, null, JasperExportType.PDF, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void generateDocxWithNestedReportTest() throws IOException, URISyntaxException {

        File outFile = File.createTempFile("releve_multiple_out_", ".docx", new File("build"));
        //outFile.deleteOnExit();

        //json data input
        List<Object> data = objectMapper.readValue(this.getClass().getResourceAsStream("/jasper/releveNote/releveNoteMultiple.json"), ArrayList.class);
        Map<String, Object> params = null;
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate("releveNote", data, null, JasperExportType.DOCX, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void generateWithTemplateNotFoundTest() throws IOException {

        //json data input
        List<Object> data = objectMapper.readValue(this.getClass().getResourceAsStream("/jasper/releveNote/releveNoteMultiple.json"), ArrayList.class);
        try {
            printerService.generate("notfound", data, null,JasperExportType.PDF, null);
            Assert.fail("Should fail");
        } catch (TemplateNotFoundException e) {
            //success
        } catch (Exception e) {
            Assert.fail("Should throw TemplateNotFoundException");
        }
    }
}
