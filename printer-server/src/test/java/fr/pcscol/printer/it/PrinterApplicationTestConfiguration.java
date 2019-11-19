package fr.pcscol.printer.it;

import fr.pcscol.printer.adapter.PrinterRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PrinterApplicationTestConfiguration {

    @Autowired
    private PrinterRestTemplate printerRestTemplate;

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RestTemplate restTemplate() {
        return printerRestTemplate.getRestTemplate();
    }

}
