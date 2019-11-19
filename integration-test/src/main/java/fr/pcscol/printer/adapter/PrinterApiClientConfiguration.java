package fr.pcscol.printer.adapter;

import fr.pcscol.printer.client.ApiClient;
import fr.pcscol.printer.client.api.PrinterApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class PrinterApiClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrinterApiClientConfiguration.class);

    private static final String printerApiBasePath = "http://%s:%s/printer/v1";

    @Value("#{systemProperties['printer-server.host']}")
    private String host;

    @Value("#{systemProperties['printer-server.tcp.8080']}")
    private String port;

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ApiClient printerApiClient(PrinterRestTemplate printerRestTemplate) {
        ApiClient printerApiClient = new ApiClient(printerRestTemplate.getRestTemplate());
        printerApiClient.setBasePath(String.format(printerApiBasePath, host, port));

        return printerApiClient;
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrinterApi getPrinterApi(ApiClient printerApiClient) {
        return new PrinterApi(printerApiClient);
    }
}
