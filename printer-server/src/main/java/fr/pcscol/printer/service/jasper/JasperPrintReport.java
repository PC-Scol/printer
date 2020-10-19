package fr.pcscol.printer.service.jasper;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.fill.JasperReportSource;
import net.sf.jasperreports.engine.fill.SimpleJasperReportSource;
import net.sf.jasperreports.repo.SimpleRepositoryResourceContext;

import java.nio.file.Path;

public class JasperPrintReport {

    private final String resourceFolder;
    private String name;
    private String url;
    private Path folder;
    private JasperReport main;
    private ReportContext context;
    private JasperReportSource source;

    public JasperPrintReport(JasperTemplateDef def, String resourceFolder) {
        this.name = def.getName();
        this.url = def.getUrl();
        this.resourceFolder = resourceFolder;
    }

    public void setFolder(Path folder) {
        this.folder = folder;
    }

    public void setMainReport(JasperReport report) {
        this.main = report;
    }

    public void setReportContext(ReportContext reportContext) {
        this.context = reportContext;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Path getFolder() {
        return folder;
    }

    public JasperReport getMain() {
        return main;
    }

    public ReportContext getContext() {
        return context;
    }

    public JasperReportSource getSource() {
        if (source == null) {
            SimpleRepositoryResourceContext fallBackContext = SimpleRepositoryResourceContext.of(resourceFolder);
            fallBackContext.setSelfAsDerivedFallback(true);
            SimpleRepositoryResourceContext context = SimpleRepositoryResourceContext.of(folder.toString(), fallBackContext);
            context.setSelfAsDerivedFallback(true);
            source = SimpleJasperReportSource.from(getMain(), folder.toString(), context);
        }
        return source;
    }
}
