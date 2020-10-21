package fr.pcscol.printer.controller.v1;

import fr.pcscol.printer.PrinterUtil;
import fr.pcscol.printer.api.PrinterApi;
import fr.pcscol.printer.api.model.FieldMetadata;
import fr.pcscol.printer.api.model.ImageFieldMetadata;
import fr.pcscol.printer.api.model.PrintMessage;
import fr.pcscol.printer.api.model.TextStylingFieldMetadata;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import fr.pcscol.printer.service.xdoc.XdocFieldMetadata;
import fr.pcscol.printer.service.xdoc.XdocPrinterService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/printer/v1")
@Api(tags = {"Printer"}, description = "Printer V1 resource")
public class PrinterV1Controller implements PrinterApi {

    @Autowired
    private XdocPrinterService printerService;

    @Value("${printer.template.base-url}")
    private String templateBaseUrl;

    public static final Function<FieldMetadata, XdocFieldMetadata> toXdocFieldMetadata = f -> {
        XdocFieldMetadata result;
        if (f instanceof ImageFieldMetadata) {
            result = new XdocImageFieldMetadataAdapter((ImageFieldMetadata) f);
        } else if (f instanceof TextStylingFieldMetadata) {
            result = new XdocTextStylingFieldMetadataAdapter((TextStylingFieldMetadata) f);
        } else {
            result = new XdocFieldMetadataAdapter(f);
        }
        return result;
    };

    @Override
    public ResponseEntity<byte[]> print(@NotNull PrintMessage body) {

        //check template url is valid and complete it if not absolute
        URL templateUrl;
        try {
            templateUrl = PrinterUtil.completeUrl(body.getTemplateUrl(), templateBaseUrl);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }

        //data to print
        Map<String, Object> data = (Map<String, Object>) body.getData();

        //metadata about fields
        List<XdocFieldMetadata> fieldMetadataList = null;
        if(body.getFieldsMetadata() != null) {
           fieldMetadataList = Optional.ofNullable(body.getFieldsMetadata()).orElse(Collections.emptyList()).stream().map(toXdocFieldMetadata).collect(Collectors.toList());
        }

        //is pdf conversion requested
        boolean convert = Boolean.TRUE.equals(body.isConvert());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try {
                //invoke generation
                printerService.generate(templateUrl, data, fieldMetadataList, convert, outputStream);
                //success response
                byte[] content = outputStream.toByteArray();
                //extract output file name
                String fileName = PrinterUtil.extractOutputFileName(templateUrl.getPath(), String.valueOf(System.currentTimeMillis()), convert);
                //return response
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=%s", fileName))
                        // Content-Type
                        .contentType(convert ? MediaType.APPLICATION_PDF : MediaType.valueOf(PrinterUtil.getMimeType(templateUrl.getFile())))
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
