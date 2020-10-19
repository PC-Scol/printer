package fr.pcscol.printer.service.jasper;

import fr.pcscol.printer.PrinterUtil;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRVisitorSupport;
import net.sf.jasperreports.repo.JasperDesignCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

@Service
public class JasperLoaderService {

    private Logger logger = LoggerFactory.getLogger(JasperLoaderService.class);

    @Autowired
    private JasperConfiguration configuration;

    public void load() throws IOException {
        for (JasperTemplateDef def : configuration.getTemplates()) {
            Path folder = Path.of(configuration.getUnzipFolder());
            URL templateUrl = PrinterUtil.completeUrl(def.getUrl(), configuration.getBaseUrl());
            PrinterUtil.unzipTemplate(templateUrl, folder);
        }
    }

    public JasperPrintReport get(String name) {
        for (JasperTemplateDef def : configuration.getTemplates()) {
            if (def.getName().equals(name)) {
                Path folder = Path.of(configuration.getUnzipFolder(), def.getName());
                return loadJasperDefinition(def, folder);
            }
        }
        throw new TemplateNotFoundException(String.format("Template with name %s is not defined.", name));
    }

    private JasperPrintReport loadJasperDefinition(JasperTemplateDef def, Path folder) {
        JasperPrintReport jasperPrintReport = new JasperPrintReport(def, configuration.getResourceFolder());
        jasperPrintReport.setFolder(folder);
        ReportContext reportContext = new SimpleReportContext();

        JasperReport report = loadFromJrxml(Path.of(folder.toString(), def.getMain()), reportContext);
        jasperPrintReport.setMainReport(report);
        jasperPrintReport.setReportContext(reportContext);
        return jasperPrintReport;

    }

    private JasperReport loadFromJrxml(Path jrxmlFilePath, ReportContext reportContext) throws TemplateNotFoundException {

        logger.debug("loading jrxml file {}", jrxmlFilePath);

        File jrxmlFile = new File(jrxmlFilePath.toUri());
        if (jrxmlFile.exists()) {
            logger.info("Try to compile from JRXML @ {}", jrxmlFilePath);
            try (InputStream in = new FileInputStream(jrxmlFile)) {
                JasperReport jasperReport = JasperCompileManager.compileReport(in);
                logger.info("JRXML file @ {} successfully compiled.", jrxmlFilePath);
                //load recursively subreports if any
                JRElementsVisitor.visitReport(jasperReport, new JRVisitorSupport() {
                    @Override
                    public void visitSubreport(JRSubreport subreport) {
                        String subReportFileName = subreport.getExpression().getText().replaceAll(PrinterUtil.QUOTE, PrinterUtil.EMPTY);
                        Path subJrxmlFilePath = Path.of(jrxmlFilePath.getParent().toString(), subReportFileName.replace(".jasper", ".jrxml"));
                        //load subreport
                        JasperReport subReport = loadFromJrxml(subJrxmlFilePath, reportContext);
                        //cache it
                        JasperDesignCache cache = JasperDesignCache.getInstance(DefaultJasperReportsContext.getInstance(), reportContext);
                        cache.set(subReportFileName, subReport);
                    }
                });
                return jasperReport;
            } catch (JRException e) {
                String err = String.format("Unable to compile JRXML @ %s.", jrxmlFilePath.toString());
                logger.error(err, e);
                throw new JasperCompilationException(err, e);
            } catch (IOException e) {
                String err = String.format("Unable to find JRXML @ %s.", jrxmlFilePath.toString());
                logger.error(err);
                throw new TemplateNotFoundException(err);
            }
        } else {
            String err = String.format("Unable to find JRXML @ %s.", jrxmlFilePath.toString());
            logger.error(err);
            throw new TemplateNotFoundException(err);
        }
    }
}
