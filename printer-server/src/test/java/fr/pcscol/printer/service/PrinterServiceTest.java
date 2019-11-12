package fr.pcscol.printer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.PersonBean;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Tests the {@link PrinterService layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PrinterServiceTest {

    @Autowired
    private PrinterService printerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${printer.template.base-url}")
    private String templateBaseUrl;


    /**
     * Tests a correct generation of a ODT document for the given valid templateUrl and data.
     * @throws IOException
     */
    @Test
    public void generateTest() throws IOException {

        File outFile = File.createTempFile("certificat_out_", ".odt", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = this.getClass().getResource("/certificat.odt");
        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, map, false, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * Tests a correct generation with PDF conversion for the given valid templateUrl and data.
     * @throws IOException
     */
    @Test
    public void generateAndConvertTest() throws IOException {

        File outFile = File.createTempFile("certificat_out_", ".pdf", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = this.getClass().getResource("/certificat.odt");
        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, map, true, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * Tests a generation failure for the non existing templateUrl.
     * @throws IOException
     */
    @Test
    public void generateWithTemplateNotFoundTest() throws IOException {

        File outFile = File.createTempFile("certificat_out_", ".odt", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = new URL("file://path/to/notfound.odt");
        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, map, false, outputStream);
                Assert.fail("Should fail");
            } catch (TemplateNotFoundException e) {
                Assert.assertThat(outFile.length(), Matchers.equalTo(0L));
            } catch (Exception e) {
                Assert.fail("Unexpected exception");
            }
        }
    }

    /**
     * Tests a generation failure for a non valid template content type.
     * @throws IOException
     */
    @Test
    public void generateWithTemplateNotAllowedTest() throws IOException {

        File outFile = File.createTempFile("certificat_out_", ".odt", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = this.getClass().getResource("/template_not_allowed.txt");
        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, map, false, outputStream);
                Assert.fail("Should fail");
            } catch (TemplateNotFoundException e) {
                Assert.assertThat(outFile.length(), Matchers.equalTo(0L));
            } catch (Exception e) {
                Assert.fail("Unexpected exception");
            }
        }
    }
}
