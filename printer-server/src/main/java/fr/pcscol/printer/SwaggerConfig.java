package fr.pcscol.printer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

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
                .build().tags( new Tag("printer", "printer operations"));
    }
}
