package fr.pcscol.printer;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.Collections;

@SpringBootApplication
@EnableSwagger2
public class PrinterApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrinterApplication.class, args);
    }


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v1")
                .apiInfo(new ApiInfo(
                        "API Printer Service",
                        "Webservice d'impression de documents",
                        "1.0.0",
                        "",
                        null,
                        "",
                        "",
                        Collections.emptyList()
                ))
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.pcscol.printer.controller"))
                .build().tags(new Tag("printer", "printer operations"));
    }
}
