package fr.pcscol.printer.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.util.HashMap;

@Component
public class PrinterClientErrorHandler extends DefaultResponseErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrinterClientErrorHandler.class);


    private final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HashMap<String, Object> error;

        try {
            error = objectMapper.readValue(response.getBody(), typeRef);
        } catch (Exception e) {
            throw new PrinterException(String.format("%s (%s)", response.getStatusCode(), response.getStatusCode().getReasonPhrase()));
        }

        LOGGER.info("RestTemplate error: {}", error);

        Object message = error.getOrDefault("message", "Erreur indéterminée");
        Object path = error.getOrDefault("path", "URL indéterminée");
        Object status = error.getOrDefault("status", "Statut indéterminée");
        Object statusMessage = error.getOrDefault("error", "Erreur indéterminée");

        throw new PrinterException(String.format(
                "%s - %s (%s) : %s",
                path.toString(),
                status.toString(),
                statusMessage.toString(),
                message.toString())
        );
    }
}
