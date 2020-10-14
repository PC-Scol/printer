package fr.pcscol.printer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.PersonBean;
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
import java.net.URISyntaxException;
import java.net.URL;
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
     * Tests a correct generation of a ODT document for the given valid templateUrl and data.
     *
     * @throws IOException
     */
    @Test
    public void generateTest() throws IOException, URISyntaxException {

        File outFile = File.createTempFile("releve_notes_out_", ".pdf", new File("build"));
        //outFile.deleteOnExit();

        //jasper file
        String templatePath = "releveNote/ReleveNoteMultiple.jasper";

        //json data input
        URL jsonFile = this.getClass().getResource("/jasper/jrxml/releveNote/releveNoteMultiple.json");
        //data
//        List<Map<String, Object>> data = objectMapper.readValue(new File(jsonFile.toURI()), ArrayList.class);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templatePath, null, JasperExportType.PDF, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}
