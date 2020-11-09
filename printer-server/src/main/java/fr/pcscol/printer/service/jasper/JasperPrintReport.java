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
    private ReportContext context;
    private JasperReportSource source;

    public JasperPrintReport(String url, String folder, String resourceFolder, JasperReport main, ReportContext context) {
        this.url = url;
        this.folder = Path.of(folder);
        this.name = this.folder.getName(this.folder.getNameCount() -1).toString();
        this.resourceFolder = resourceFolder;
        this.context = context;

        SimpleRepositoryResourceContext fallBackContext = SimpleRepositoryResourceContext.of(resourceFolder);
        SimpleRepositoryResourceContext resourceContext = SimpleRepositoryResourceContext.of(folder, fallBackContext);
        resourceContext.setSelfAsDerivedFallback(true);
        source = SimpleJasperReportSource.from(main, folder, resourceContext);
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

    public ReportContext getContext() {
        return context;
    }

    public JasperReportSource getSource() {
        return source;
    }
}
