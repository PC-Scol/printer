package fr.pcscol.printer.controller;

import fr.pcscol.printer.api.PrinterApi;
import fr.pcscol.printer.api.model.PrintMessage;
import fr.pcscol.printer.service.PrinterService;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/printer/v1")
public class PrinterController implements PrinterApi {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private PrinterService printerService;

    @Autowired
    private URLHelper urlHelper;

    private MediaType getMediaType(String fileName) {
        String mimeType = servletContext.getMimeType(fileName);
        try {
            MediaType mediaType = MediaType.parseMediaType(mimeType);
            return mediaType;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @Override
    public ResponseEntity<byte[]> print(@NotNull PrintMessage body) {

        //check template url is valid and complete it if not absolute
        URL templateUrl;
        try {
            templateUrl = urlHelper.completeUrl(body.getTemplateUrl());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }

        //data to print
        Map<String, Object> data = (Map<String, Object>) body.getData();
        //is pdf conversion requested
        boolean convert = body.isConvert();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try {
                printerService.generate(templateUrl, data, convert, outputStream);
                //success response
                byte[] content = outputStream.toByteArray();
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=test.pdf")
                        // Content-Type
                        .contentType(convert == true ? MediaType.APPLICATION_PDF : getMediaType(templateUrl.getFile()))
                        .contentLength(content.length)
                        .body(content);
            } catch (TemplateNotFoundException e) {
                //template not found or not applicable
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, e.getMessage(), e);
            } catch (DocumentGenerationException e) {
                //error occured during generation
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
}
