package fr.pcscol.printer.it;

import fr.pcscol.printer.PersonBean;
import fr.pcscol.printer.client.api.PrinterApi;
import fr.pcscol.printer.client.api.model.PrintMessage;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientResponseException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Tests the client/server integration by invoking the real WS.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PrinterApplicationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PrinterApi printerApi;

    private Boolean keepFilesEnv = Boolean.valueOf(System.getenv("keepFiles"));

    /**
     * Tests the client/server integration by invoking the real WS.
     * @throws IOException
     */
    @Test
    public void print_OKTest() throws IOException {

        File outFile = File.createTempFile("PrinterApplicationIntegrationTest_out_", ".pdf", new File("build"));
        if(!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        //set api base path
        printerApi.getApiClient().setBasePath(String.format("http://localhost:%d/printer/v1", port));

        //build PrintMessage
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        PrintMessage printMessage = new PrintMessage().templateUrl("certificat.odt").data(personBean).convert(true);

        byte[] content = printerApi.print(printMessage);
        try(BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))){
            outputStream.write(content);
        }
        Assert.assertThat(content.length, Matchers.greaterThan(0));

    }

    @Test
    public void print_Error404Test() throws Exception {

        File outFile = File.createTempFile("PrinterApplicationIntegrationTest_out_", ".pdf", new File("build"));
        if(!keepFilesEnv) {
            outFile.deleteOnExit();
        }

        //set api base path
        printerApi.getApiClient().setBasePath(String.format("http://localhost:%d/printer/v1", port));

        //build PrintMessage
        PersonBean personBean = new PersonBean("Jean", "Dupont");
        PrintMessage printMessage = new PrintMessage().templateUrl("template_not_allowed.txt").data(personBean).convert(true);

        try {
            printerApi.print(printMessage);
            Assert.fail();
        }catch (RestClientResponseException e){
            Assert.assertThat(e.getRawStatusCode(), Matchers.equalTo(404));
        }


    }
}
