package fr.pcscol.printer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Component
public class URLHelper {

    @Value("${printer.template.base-url}")
    private String templateBaseUrl;

    public URL completeUrl(String templateUrl) throws MalformedURLException {
        URI uri = null;
        try {
            uri = new URI(templateUrl);
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Provided URL format is not correct");
        }
        if (uri.isAbsolute()) {
            return new URL(templateUrl);
        } else {
            return new URL(templateBaseUrl + templateUrl);
        }
    }
}
