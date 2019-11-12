package fr.pcscol.printer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.PersonBean;
import fr.pcscol.printer.client.api.model.PrintMessage;
import fr.pcscol.printer.service.PrinterService;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
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
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link PrinterController} layer.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PrinterApplicationMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrinterService printerService;

    @Value("${printer.template.base-url}")
    private String templateBaseUrl;

    private boolean keepFilesEnv = Boolean.valueOf(System.getenv("keepFiles"));

    private static final URLStreamHandler urlStreamHandler = TomcatURLStreamHandlerFactory.getInstance().createURLStreamHandler("classpath");

    @Test
    public void print_OkTest() throws Exception {

        File outFile = File.createTempFile("PrinterApplicationMockMvcTest_out_", ".pdf", new File("build"));
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
                        byte[] generatedBytes = PrinterApplicationMockMvcTest.class.getResourceAsStream("/out.pdf").readAllBytes();
                        //write generated to outputstream arg
                        ((OutputStream) invocation.getArgument(3)).write(generatedBytes);
                        //write response to outFile
                        outputStream.write(generatedBytes);
                        return null;
                    }
            ).when(printerService).generate(eq(new URL(null, templateBaseUrl + "certificat.odt", TomcatURLStreamHandlerFactory.getInstance().createURLStreamHandler("classpath"))),
                    eq(map), eq(true), any(OutputStream.class));

            //invoke WS
            PrintMessage printMessage = new PrintMessage().templateUrl("certificat.odt").data(personBean).convert(true);
            String body = objectMapper.writeValueAsString(printMessage);
            mvc.perform(post("/printer/v1/print").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    //check content types
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                    //compare content bytes
                    .andExpect(content().bytes(new FileInputStream(outFile).readAllBytes()))
            ;
        }
    }

    @Test
    public void print_Error400Test() throws Exception {

        File outFile = File.createTempFile("PrinterApplicationMockMvcTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {

            //data
            PersonBean personBean = new PersonBean("Jean", "Dupont");
            Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

            //invoke WS
            PrintMessage printMessage = new PrintMessage().templateUrl("-|*/").data(personBean).convert(true);
            String body = objectMapper.writeValueAsString(printMessage);
            mvc.perform(post("/printer/v1/print").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("Provided URL format is not correct"));

        }
    }

    @Test
    public void print_Error404Test() throws Exception {

        File outFile = File.createTempFile("PrinterApplicationMockMvcTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {

            //data
            PersonBean personBean = new PersonBean("Jean", "Dupont");
            Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

            //mock printService call
            doThrow(new TemplateNotFoundException("Template not found")).when(printerService).generate(eq(new URL(null, templateBaseUrl + "unknown.odt", TomcatURLStreamHandlerFactory.getInstance().createURLStreamHandler("classpath"))),
                    eq(map), eq(true), any(OutputStream.class));

            //invoke WS
            PrintMessage printMessage = new PrintMessage().templateUrl("unknown.odt").data(personBean).convert(true);
            String body = objectMapper.writeValueAsString(printMessage);
            mvc.perform(post("/printer/v1/print").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(status().reason("Template not found"));

        }
    }

    @Test
    public void print_Error500Test() throws Exception {

        File outFile = File.createTempFile("PrinterApplicationMockMvcTest_out_", ".pdf", new File("build"));
        if (!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {

            //data
            PersonBean personBean = new PersonBean("Jean", "Dupont");
            Map<String, Object> map = objectMapper.convertValue(personBean, Map.class);

            //mock printService call
            URL url = new URL(null, templateBaseUrl + "certificat.odt", urlStreamHandler);
            doThrow(new DocumentGenerationException("An error occured during document generation")).when(printerService).generate(eq(url),
                    eq(map), eq(true), any(OutputStream.class));

            //invoke WS
            PrintMessage printMessage = new PrintMessage().templateUrl("certificat.odt").data(personBean).convert(true);
            String body = objectMapper.writeValueAsString(printMessage);
            mvc.perform(post("/printer/v1/print").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isInternalServerError())
                    .andExpect(status().reason("An error occured during document generation"));

        }
    }
}
