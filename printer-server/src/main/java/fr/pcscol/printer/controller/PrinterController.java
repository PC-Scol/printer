package fr.pcscol.printer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.api.PrinterApi;
import fr.pcscol.printer.api.model.PrintMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@RestController
@RequestMapping("/printer/v1")
public class PrinterController implements PrinterApi {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MediaType getMediaType(String fileName){
        String mineType = servletContext.getMimeType(fileName);
        try {
            MediaType mediaType = MediaType.parseMediaType(mineType);
            return mediaType;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @Override
    public ResponseEntity<byte[]> print(PrintMessage body) {
        URL url = this.getClass().getResource("/test.pdf");

        try(InputStream inputStream = url.openStream()) {

            byte[] content = inputStream.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=test.pdf")
                    // Content-Type
                    .contentType(getMediaType("test.pdf"))
                    .contentLength(content.length)
                    .body(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
