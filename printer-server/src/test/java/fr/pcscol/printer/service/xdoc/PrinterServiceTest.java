package fr.pcscol.printer.service.xdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import fr.opensagres.xdocreport.document.images.ClassPathImageProvider;
import fr.pcscol.printer.PersonBean;
import fr.pcscol.printer.api.model.FieldMetadata;
import fr.pcscol.printer.api.model.ImageFieldMetadata;
import fr.pcscol.printer.api.model.TextStylingFieldMetadata;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the {@link PrinterService} layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PrinterServiceTest {

    static {
        TomcatURLStreamHandlerFactory.getInstance();
    }

    @Autowired
    private PrinterService printerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${printer.template.base-url}")
    private String templateBaseUrl;


    /**
     * Tests a correct generation of a ODT document for the given valid templateUrl and data.
     *
     * @throws IOException
     */
    @Test
    public void generateTest() throws IOException {

        File outFile = File.createTempFile("certificat_out_", ".odt", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/certificat.odt");
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
     *
     * @throws IOException
     */
    @Test
    public void generateAndConvertTest() throws IOException {

        File outFile = File.createTempFile("certificat_out_", ".pdf", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/certificat.odt");
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
     *
     * @throws IOException
     */
    @Test
    public void generateWithTemplateNotFoundTest() throws IOException {

        //templateUrl
        URL templateUrl = new URL("file://path/to/notfound.odt");
        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

        try {
            printerService.generate(templateUrl, map, false, null);
            Assert.fail("Should fail");
        } catch (TemplateNotFoundException e) {
            //success
        } catch (Exception e) {
            Assert.fail("Should throw TemplateNotFoundException");
        }

    }

    /**
     * Tests a generation failure for a non valid template content type.
     */
    @Test
    public void generateWithTemplateNotAllowedTest() {

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/template_not_allowed.txt");
        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

        try {
            printerService.generate(templateUrl, map, false, null);
            Assert.fail("Should fail");
        } catch (TemplateNotFoundException e) {
            //success
        } catch (Exception e) {
            Assert.fail("Should throw TemplateNotFoundException");
        }
    }

    /**
     * Tests generation with metadata fields
     *
     * @throws IOException
     */
    @Test
    public void generateWithFieldsMetadataTest() throws IOException {
        File outFile = File.createTempFile("metadata_out_", ".odt", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/template_metadata.odt");
        //data
        Map<String, Object> map = new HashMap<>();
        map.put("o1", new HashMap<>());
        map.put("logo", new ClassPathImageProvider(PrinterServiceTest.class, "/xdoc/logo.gif"));
        map.put("items", Lists.newArrayList("item1", "item2", "item3"));
        map.put("url", "http://google.com");
        map.put("link", "<a href=\"${url}\">Google</a> ");

        //metadata
        List<FieldMetadata> fieldMetadataList = new ArrayList<>();
        fieldMetadataList.add(new FieldMetadata().fieldName("items").listType(true));
        fieldMetadataList.add(new ImageFieldMetadata().nullImageBehaviour(ImageFieldMetadata.NullImageBehaviourEnum.KEEPIMAGETEMPLATE).fieldName("logo"));
        fieldMetadataList.add(new TextStylingFieldMetadata().syntaxKind(TextStylingFieldMetadata.SyntaxKindEnum.HTML).syntaxWithDirective(true).fieldName("link"));

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, map, fieldMetadataList, false, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * Tests generation with metadata fields
     *
     * @throws IOException
     */
    @Test
    public void generateWithFieldsMetadataForNestedImageBase64Test() throws IOException {
        File outFile = File.createTempFile("metadata_nesting_out_", ".odt", new File("build"));
        outFile.deleteOnExit();

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/template_metadata_nesting.odt");
        //data
        String image;
        try (InputStream inputStream = this.getClass().getResourceAsStream("/xdoc/logo.gif")) {
            image = Base64.encode(inputStream.readAllBytes());
        }
        PrinterContext printerContext = new PrinterContext();
        printerContext.put("o1.o2.logo", image);
        printerContext.put("items", Lists.newArrayList("item1", "item2", "item3"));
        printerContext.put("o1.items", Lists.newArrayList("item1", "item2", "item3"));
        printerContext.put("o1.url", "http://google.com");
        printerContext.put("o1.link", "<a href=\"${o1.url}\">Google</a> ");

        //metadata
        List<FieldMetadata> fieldMetadataList = new ArrayList<>();
        fieldMetadataList.add(new FieldMetadata().fieldName("items").listType(true));
        fieldMetadataList.add(new FieldMetadata().fieldName("o1.items").listType(true));
        fieldMetadataList.add(new ImageFieldMetadata().nullImageBehaviour(ImageFieldMetadata.NullImageBehaviourEnum.KEEPIMAGETEMPLATE).fieldName("o1.o2.logo"));
        fieldMetadataList.add(new TextStylingFieldMetadata().syntaxKind(TextStylingFieldMetadata.SyntaxKindEnum.HTML).syntaxWithDirective(true).fieldName("o1.link"));

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {
            try {
                printerService.generate(templateUrl, printerContext.getContextMap(), fieldMetadataList, false, outputStream);
                Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    /**
     * Tests generation with metadata fields error : expected list is not
     */
    @Test
    public void generateWithFieldsMetadataNotListErrorTest() {

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/template_metadata.odt");
        //data
        Map<String, Object> map = new HashMap<>();
        map.put("logo", new ClassPathImageProvider(PrinterServiceTest.class, "/xdoc/logo.gif"));
        //NOT LIST
        map.put("items", "singleItem");

        //metadata
        List<FieldMetadata> fieldMetadataList = new ArrayList<>();
        fieldMetadataList.add(new FieldMetadata().fieldName("items").listType(true));
        fieldMetadataList.add(new ImageFieldMetadata().nullImageBehaviour(ImageFieldMetadata.NullImageBehaviourEnum.KEEPIMAGETEMPLATE).fieldName("logo"));

        try {
            printerService.generate(templateUrl, map, fieldMetadataList, false, null);
            Assert.fail("Should throw DocumentGenerationException");
        } catch (DocumentGenerationException e) {
            //success
        } catch (Exception e) {
            Assert.fail("Should throw DocumentGenerationException");
        }

    }

    /**
     * Tests generation with metadata fields error : image not found
     */
    @Test
    public void generateWithFieldsMetadataImageNotFoundErrorTest() {

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/template_metadata.odt");
        //data
        Map<String, Object> map = new HashMap<>();
        map.put("logo", new ClassPathImageProvider(PrinterServiceTest.class, "/unknown.gif"));
        map.put("items", Lists.newArrayList());

        //metadata
        List<FieldMetadata> fieldMetadataList = new ArrayList<>();
        fieldMetadataList.add(new FieldMetadata().fieldName("items").listType(true));
        fieldMetadataList.add(new ImageFieldMetadata().nullImageBehaviour(ImageFieldMetadata.NullImageBehaviourEnum.THROWSERROR).fieldName("logo"));

        try {
            printerService.generate(templateUrl, map, fieldMetadataList, false, null);
            Assert.fail("Should throw DocumentGenerationException");
        } catch (DocumentGenerationException e) {
            //success
        } catch (Exception e) {
            Assert.fail("Should throw DocumentGenerationException");
        }

    }

    /**
     * Tests generation with metadata fields error : image badly encoded
     */
    @Test
    public void generateWithFieldsMetadataImageBadlyEncodedErrorTest() {

        //templateUrl
        URL templateUrl = this.getClass().getResource("/xdoc/template_metadata.odt");
        //data
        PrinterContext printerContext = new PrinterContext();
        printerContext.put("logo", new Object());
        printerContext.put("items", Lists.newArrayList("item1", "item2", "item3"));

        //metadata
        List<FieldMetadata> fieldMetadataList = new ArrayList<>();
        fieldMetadataList.add(new FieldMetadata().fieldName("items").listType(true));
        fieldMetadataList.add(new ImageFieldMetadata().nullImageBehaviour(ImageFieldMetadata.NullImageBehaviourEnum.THROWSERROR).fieldName("logo"));

        try {
            printerService.generate(templateUrl, printerContext.getContextMap(), fieldMetadataList, false, null);
            Assert.fail("Should throw DocumentGenerationException");
        } catch (DocumentGenerationException e) {
            //success
        } catch (Exception e) {
            Assert.fail("Should throw DocumentGenerationException");
        }


    }
}
