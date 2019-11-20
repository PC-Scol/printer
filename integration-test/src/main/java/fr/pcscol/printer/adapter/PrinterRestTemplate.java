package fr.pcscol.printer.adapter;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cette classe est là pour ne pas potentiellement partager le bean restTemplate.
 */
@Component
public class PrinterRestTemplate {

    private RestTemplate restTemplate;

    public PrinterRestTemplate(RestTemplateBuilder restTemplateBuilder, PrinterClientErrorHandler handler) {
        restTemplate = restTemplateBuilder
                .errorHandler(handler)
                .build();
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
