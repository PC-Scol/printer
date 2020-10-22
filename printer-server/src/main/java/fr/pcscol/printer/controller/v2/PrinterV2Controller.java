package fr.pcscol.printer.controller.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pcscol.printer.PrinterUtil;
import fr.pcscol.printer.api.v2.PrinterApi;
import fr.pcscol.printer.api.v2.model.JasperPrintMessage;
import fr.pcscol.printer.api.v2.model.XdocPrintMessage;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import fr.pcscol.printer.service.jasper.JasperExportType;
import fr.pcscol.printer.service.jasper.JasperPrinterService;
import fr.pcscol.printer.service.xdoc.XdocFieldMetadata;
import fr.pcscol.printer.service.xdoc.XdocPrinterService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/printer/v2")
@Api(tags = {"Printer"}, description = "Printer V2 resource")
public class PrinterV2Controller implements PrinterApi {

    @Autowired
    private XdocPrinterService xdocPrinterService;

    @Autowired
    private JasperPrinterService jasperPrinterService;

    @Value("${printer.template.base-url}")
    private String templateBaseUrl;

    @Autowired
    private ObjectMapper objectMapper;

    public static final Function<fr.pcscol.printer.api.v2.model.FieldMetadata, XdocFieldMetadata> toXdocFieldMetadata = f -> {
        XdocFieldMetadata result;
        if (f instanceof fr.pcscol.printer.api.v2.model.ImageFieldMetadata) {
            result = new fr.pcscol.printer.controller.v2.XdocImageFieldMetadataAdapter((fr.pcscol.printer.api.v2.model.ImageFieldMetadata) f);
        } else if (f instanceof fr.pcscol.printer.api.v2.model.TextStylingFieldMetadata) {
            result = new fr.pcscol.printer.controller.v2.XdocTextStylingFieldMetadataAdapter((fr.pcscol.printer.api.v2.model.TextStylingFieldMetadata) f);
        } else {
            result = new fr.pcscol.printer.controller.v2.XdocFieldMetadataAdapter(f);
        }
        return result;
    };

    @Override
    public ResponseEntity<byte[]> jasperPrint(@NotNull JasperPrintMessage body) {

        String templateName = body.getTemplateName();
        if (StringUtils.isBlank(templateName)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Template name must be provided.");
        }

        //data to print
        JsonNode data;
        try {
            data = objectMapper.convertValue(body.getData(), JsonNode.class);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bad json data format.");
        }

        //additional parameters
        Map<String, Object> parameters = body.getParameters();

        //export type
        JasperExportType exportType;
        try {
            exportType = JasperExportType.valueOf(body.getExportType().toString());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Bad export type provided.", e);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try {
                //invoke generation
                jasperPrinterService.generate(templateName, data, parameters, exportType, outputStream);
                //success response
                byte[] content = outputStream.toByteArray();
                //extract output file name
                String fileName = new StringBuilder(templateName).append(PrinterUtil.UNDERSCORE).append(System.currentTimeMillis()).append(PrinterUtil.DOT).append(exportType.toString().toLowerCase()).toString();
                //return response
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=%s", fileName))
                        // Content-Type
                        .contentType(MediaType.valueOf(PrinterUtil.getMimeType(exportType)))
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

    @Override
    public ResponseEntity<byte[]> xdocPrint(@NotNull XdocPrintMessage body) {

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
        if (body.getFieldsMetadata() != null) {
            fieldMetadataList = Optional.ofNullable(body.getFieldsMetadata()).orElse(Collections.emptyList()).stream().map(toXdocFieldMetadata).collect(Collectors.toList());
        }

        //is pdf conversion requested
        boolean convert = Boolean.TRUE.equals(body.isConvert());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try {
                //invoke generation
                xdocPrinterService.generate(templateUrl, data, fieldMetadataList, convert, outputStream);
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
