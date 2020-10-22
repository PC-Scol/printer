package fr.pcscol.printer.service.jasper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "printer.jasper")
public class JasperConfiguration {

    private String baseUrl;
    private String unzipFolder;
    private String resourceFolder;

    private List<JasperTemplateDef> templates;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUnzipFolder() {
        return unzipFolder;
    }

    public void setUnzipFolder(String unzipFolder) {
        this.unzipFolder = unzipFolder;
    }

    public List<JasperTemplateDef> getTemplates() {
        return templates;
    }

    public void setTemplates(List<JasperTemplateDef> templates) {
        this.templates = templates;
    }

    public String getResourceFolder() {
        return resourceFolder;
    }

    public void setResourceFolder(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }
}
