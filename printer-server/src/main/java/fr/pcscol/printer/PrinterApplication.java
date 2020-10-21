package fr.pcscol.printer;

import com.fasterxml.classmate.TypeResolver;
import fr.pcscol.printer.api.model.ImageFieldMetadata;
import fr.pcscol.printer.api.model.TextStylingFieldMetadata;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@SpringBootApplication
@EnableSwagger2
public class PrinterApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrinterApplication.class, args);
    }

    @Bean
    public Docket apiV1() {

        TypeResolver typeResolver = new TypeResolver();

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v1")
                .apiInfo(new ApiInfo(
                        "Printer Service API",
                        "Generates documents (odt, docx, doc, pdf) by merging a template (odt, docx, doc) and a data model.",
                        "1.0.0",
                        "",
                        null,
                        "",
                        "",
                        Collections.emptyList()
                ))
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.pcscol.printer.controller.v1"))
                .build()
                .additionalModels(typeResolver.resolve(ImageFieldMetadata.class), typeResolver.resolve(TextStylingFieldMetadata.class));
    }

    @Bean
    public Docket apiV2() {

        TypeResolver typeResolver = new TypeResolver();

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v2")
                .apiInfo(new ApiInfo(
                        "Printer Service API",
                        "Generates documents (odt, docx, doc, pdf) by merging a template (Xdoc or Jasper jrxml) and a data model.",
                        "2.0.0",
                        "",
                        null,
                        "",
                        "",
                        Collections.emptyList()
                ))
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.pcscol.printer.controller.v2"))
                .build()
                .additionalModels(
                        typeResolver.resolve(fr.pcscol.printer.api.v2.model.ImageFieldMetadata.class),
                        typeResolver.resolve(fr.pcscol.printer.api.v2.model.TextStylingFieldMetadata.class));
    }
}
