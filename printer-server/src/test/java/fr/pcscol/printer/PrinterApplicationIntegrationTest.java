package fr.pcscol.printer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.client.ApiClient;
import fr.pcscol.printer.client.api.PrinterApi;
import fr.pcscol.printer.client.api.model.PrintMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PrinterApplicationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PrinterApi printerApi;



    @Test
    public void printTest() throws Exception {

        PersonBean personBean = new PersonBean("Jean", "Dupont");
        PrintMessage printMessage = new PrintMessage().templateUrl("test.pdf").data(personBean);

        printerApi.getApiClient().setBasePath(String.format("http://localhost:%d/printer/v1", port));

        byte[] fileBytes = printerApi.print(printMessage);
        try(FileOutputStream outputStream = new FileOutputStream("build/out.pdf")){
            outputStream.write(fileBytes);
        }

    }
}
