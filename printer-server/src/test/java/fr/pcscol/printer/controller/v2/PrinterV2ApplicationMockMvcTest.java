package fr.pcscol.printer.controller.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.model.PersonBean;
import fr.pcscol.printer.PrinterUtil;
import fr.pcscol.printer.api.v2.model.FreemarkerPrintMessage;
import fr.pcscol.printer.api.v2.model.JasperPrintMessage;
import fr.pcscol.printer.api.v2.model.XdocPrintMessage;
import fr.pcscol.printer.controller.v1.PrinterV1Controller;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import fr.pcscol.printer.service.freemarker.FreemarkerPrinterService;
import fr.pcscol.printer.service.jasper.JasperExportType;
import fr.pcscol.printer.service.jasper.JasperPrinterService;
import fr.pcscol.printer.service.xdoc.XdocPrinterService;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.*;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link PrinterV1Controller} layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PrinterV2ApplicationMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private XdocPrinterService xdocPrinterService;

    @MockBean
    private JasperPrinterService jasperPrinterService;

    @MockBean
    private FreemarkerPrinterService freemarkerPrinterService;

    @Value("${printer.xdoc.base-url}")
    private String xdocTemplateBaseUrl;

    @Value("${printer.jasper.base-url}")
    private String jasperTemplateBaseUrl;

    private static final Boolean keepFilesEnv = Boolean.valueOf(System.getenv("keepFiles"));

    static {
        TomcatURLStreamHandlerFactory.getInstance();
    }

    @Before
    public void init(){
        when(xdocPrinterService.getTemplateBaseUrl()).thenReturn(xdocTemplateBaseUrl);
        when(jasperPrinterService.getTemplateBaseUrl()).thenReturn(jasperTemplateBaseUrl);
    }


    @Test
    public void xdocPrint_OkTest() throws Exception {

        File outFile = File.createTempFile("PrinterV2ApplicationMockMvcTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {

            //data
            PersonBean personBean = new PersonBean("Jean", "Dupont");
            Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

            //mock printService call
            doAnswer((Answer<Void>) invocation ->
                    {
                        byte[] generatedBytes = PrinterV2ApplicationMockMvcTest.class.getResourceAsStream("/xdoc/out.pdf").readAllBytes();
                        //write generated to outputstream arg
                        ((OutputStream) invocation.getArgument(4)).write(generatedBytes);
                        //write response to outFile
                        outputStream.write(generatedBytes);
                        return null;
                    }
            ).when(xdocPrinterService).generate(eq(PrinterUtil.completeUrl("xdoc/certificat.odt", xdocTemplateBaseUrl)), eq(map), isNull(), eq(true), any(OutputStream.class));

            //invoke WS
            XdocPrintMessage printMessage = new XdocPrintMessage().templateUrl("xdoc/certificat.odt").data(personBean).convert(true);
            String body = objectMapper.writeValueAsString(printMessage);
            mvc.perform(post("/printer/v2/print/xdoc").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    //check content types
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                    //compare content bytes
                    .andExpect(content().bytes(new FileInputStream(outFile).readAllBytes()))
            ;
            Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
        }
    }

    @Test
    public void xdocPrint_Error400Test() throws Exception {

        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

        //invoke WS
        XdocPrintMessage printMessage = new XdocPrintMessage().templateUrl("-|*/").data(personBean).convert(true);
        String body = objectMapper.writeValueAsString(printMessage);
        mvc.perform(post("/printer/v2/print/xdoc").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Provided URL format is not correct : -|*/"));

    }

    @Test
    public void xdocprint_Error404Test() throws Exception {

        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

        //mock printService call
        doThrow(new TemplateNotFoundException("Template not found")).when(xdocPrinterService).generate(
                eq(PrinterUtil.completeUrl("unknown.odt", xdocTemplateBaseUrl)), eq(map), isNull(), eq(true), any(OutputStream.class));

        //invoke WS
        XdocPrintMessage printMessage = new XdocPrintMessage().templateUrl("unknown.odt").data(personBean).convert(true);
        String body = objectMapper.writeValueAsString(printMessage);
        mvc.perform(post("/printer/v2/print/xdoc").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Template not found"));
    }

    @Test
    public void xdocPrint_Error500Test() throws Exception {

        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

        //mock printService call
        doThrow(new DocumentGenerationException("An error occured during document generation")).when(xdocPrinterService).generate(
                eq(PrinterUtil.completeUrl("xdoc/certificat.odt", xdocTemplateBaseUrl)), eq(map), isNull(), eq(true), any(OutputStream.class));

        //invoke WS
        XdocPrintMessage printMessage = new XdocPrintMessage().templateUrl("xdoc/certificat.odt").data(personBean).convert(true);
        String body = objectMapper.writeValueAsString(printMessage);
        mvc.perform(post("/printer/v2/print/xdoc").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("An error occured during document generation"));
    }

    @Test
    public void jasperPrint_OkTest() throws Exception {

        File outFile = File.createTempFile("PrinterV2ApplicationMockMvcTest_jasper_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {

            //data
            PersonBean personBean = new PersonBean("Jean", "Dupont");
            JsonNode data = objectMapper.convertValue(personBean, JsonNode.class);

            //mock printService call
            doAnswer((Answer<Void>) invocation ->
                    {
                        byte[] generatedBytes = PrinterV2ApplicationMockMvcTest.class.getResourceAsStream("/xdoc/out.pdf").readAllBytes();
                        //write generated to outputstream arg
                        ((OutputStream) invocation.getArgument(4)).write(generatedBytes);
                        //write response to outFile
                        outputStream.write(generatedBytes);
                        return null;
                    }
            ).when(jasperPrinterService).generate(eq(PrinterUtil.completeUrl("certificat.zip", jasperTemplateBaseUrl)), eq(data), isNull(), eq(JasperExportType.PDF), any(OutputStream.class));

            //invoke WS
            JasperPrintMessage printMessage = new JasperPrintMessage().templateUrl("certificat.zip").data(personBean).exportType(JasperPrintMessage.ExportTypeEnum.PDF);
            String body = objectMapper.writeValueAsString(printMessage);
            mvc.perform(post("/printer/v2/print/jasper").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    //check content types
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                    //compare content bytes
                    .andExpect(content().bytes(new FileInputStream(outFile).readAllBytes()))
            ;
            Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
        }
    }

    @Test
    public void jasperPrint_Error400Test() throws Exception {

        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");

        //invoke WS
        JasperPrintMessage printMessage = new JasperPrintMessage().templateUrl("").data(personBean);
        String body = objectMapper.writeValueAsString(printMessage);
        mvc.perform(post("/printer/v2/print/jasper").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Provided URL format is not correct : "));

    }

    @Test
    public void jasperPrint_Error404Test() throws Exception {

        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        JsonNode data = objectMapper.convertValue(personBean, JsonNode.class);

        //mock printService call
        doThrow(new TemplateNotFoundException("Template not found")).when(jasperPrinterService).generate(
                eq(PrinterUtil.completeUrl("unknown.odt", jasperTemplateBaseUrl)), eq(data), isNull(), eq(JasperExportType.PDF), any(OutputStream.class));

        //invoke WS
        JasperPrintMessage printMessage = new JasperPrintMessage().templateUrl("unknown.odt").data(personBean);
        String body = objectMapper.writeValueAsString(printMessage);
        mvc.perform(post("/printer/v2/print/jasper").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Template not found"));
    }

    @Test
    public void jasperPrint_Error500Test() throws Exception {

        //data
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        JsonNode data = objectMapper.convertValue(personBean, JsonNode.class);

        //mock printService call
        doThrow(new DocumentGenerationException("An error occured during document generation")).when(jasperPrinterService).generate(
                eq(PrinterUtil.completeUrl("certificat.zip", jasperTemplateBaseUrl)), eq(data), isNull(), eq(JasperExportType.ODT), any(OutputStream.class));

        //invoke WS
        JasperPrintMessage printMessage = new JasperPrintMessage().templateUrl("certificat.zip").data(personBean).exportType(JasperPrintMessage.ExportTypeEnum.ODT);
        String body = objectMapper.writeValueAsString(printMessage);
        mvc.perform(post("/printer/v2/print/jasper").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(status().reason("An error occured during document generation"));
    }

    @Test
    public void freemarkerPrint_OkTest() throws Exception {

        File outFile = File.createTempFile("PrinterV2ApplicationMockMvcTest_out_", ".csv", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {

            //data
            Object personBean = new PersonBean("Jean", "Dupont");

            //mock printService call
            doAnswer((Answer<Void>) invocation ->
                    {
                        byte[] generatedBytes = PrinterV2ApplicationMockMvcTest.class.getResourceAsStream("/freemarker/out.csv").readAllBytes();
                        //write generated to outputstream arg
                        ((OutputStream) invocation.getArgument(2)).write(generatedBytes);
                        //write response to outFile
                        outputStream.write(generatedBytes);
                        outputStream.flush();
                        return null;
                    }
            ).when(freemarkerPrinterService).generate(eq("certificat.csv"), any(Object.class), any(OutputStream.class));

            //invoke WS
            FreemarkerPrintMessage printMessage = new FreemarkerPrintMessage().templateName("certificat.csv").data(personBean);
            String body = objectMapper.writeValueAsString(printMessage);
            mvc.perform(post("/printer/v2/print/freemarker").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    //check content types
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                    //compare content bytes
                    .andExpect(content().bytes(new FileInputStream(outFile).readAllBytes()))
            ;
            Assert.assertThat(outFile.length(), Matchers.greaterThan(0L));
        }
    }
}
