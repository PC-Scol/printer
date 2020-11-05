package fr.pcscol.printer.service.freemarker;

import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

@Service
public class FreemarkerPrinterService {

    private Logger logger = LoggerFactory.getLogger(FreemarkerPrinterService.class);

    @Value("${printer.freemarker.base-path}")
    private String basePath;

    private Configuration configuration;

    @PostConstruct
    public void init() {

        try {
            configuration = new Configuration(Configuration.VERSION_2_3_29);
            configuration.setDirectoryForTemplateLoading(new File(basePath));
            configuration.setDefaultEncoding("UTF-8");
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }catch (IOException e){
            logger.warn("Templates base directory not found !!!");
        }
    }

    public void generate(String templateName, Object data, OutputStream outputStream) throws TemplateNotFoundException, DocumentGenerationException {
        try {
            Template template = configuration.getTemplate(templateName);
            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                template.process(data, writer);
            } catch (Exception e) {
                logger.error("An error occured during document generation.", e);
                throw new DocumentGenerationException("An error occured during document generation", e);
            }
        } catch (Exception e) {
            logger.error("Unable to load template {}", templateName, e);
            throw new TemplateNotFoundException(String.format("Unable to load template %s.", templateName), e);
        }
    }


}
