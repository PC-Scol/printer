package fr.pcscol.printer.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.pcscol.printer.PrinterUtil;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRVisitorSupport;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class JasperPrinterService {

    @Value("${printer.jr.jasper.base-url}")
    private String jasperBaseUrl;

    @Value("${printer.jr.jrxml.base-url}")
    private String jrxmlBaseUrl;

    private Cache<String, JasperReport> reportCache = CacheBuilder.newBuilder().build();

    public static final String PRINTING = "PRINTING";
    private Logger logger = LoggerFactory.getLogger(JasperPrinterService.class);

    public void generate(String jasperFile, Map<String, Object> data, JasperExportType exportType, OutputStream outputStream) throws TemplateNotFoundException, DocumentGenerationException {

        logger.debug("New document generation is requested with template={}, data={}, exportType={}", jasperFile, data, exportType);

        try {
            JasperReport templateReport = getTemplateReport(jasperFile);

            JasperPrint jasperPrint = JasperFillManager.fillReport(templateReport, data);

            JRAbstractExporter exporter = getExporter(exportType);
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            logger.debug("New document generated with template {}", jasperFile);
        } catch (Exception e) {
            logger.error("An error occured during document generation.", e);
            throw new DocumentGenerationException("An error occured during document generation", e);
        }

    }

    private JRAbstractExporter getExporter(JasperExportType exportType) {
        JRAbstractExporter exporter;
        switch (exportType) {
            case PDF:
                exporter = new JRPdfExporter();
                //report config
                SimplePdfReportConfiguration reportConfig
                        = new SimplePdfReportConfiguration();
                reportConfig.setSizePageToContent(true);
                reportConfig.setForceLineBreakPolicy(false);
                //export config
                SimplePdfExporterConfiguration exportConfig
                        = new SimplePdfExporterConfiguration();
                exportConfig.setAllowedPermissionsHint(PRINTING);
                exporter.setConfiguration(reportConfig);
                exporter.setConfiguration(exportConfig);
                break;
            case XLS:
                exporter = new JRXlsxExporter();
                break;
            case DOCX:
                exporter = new JRDocxExporter();
                break;
            default:
                throw new UnsupportedOperationException("exportType is not supported");
        }
        return exporter;
    }

    private JasperReport getTemplateReport(String jasperFile) {
        try {
            return reportCache.get(jasperFile, () -> loadTemplateReport(jasperFile));
        } catch (ExecutionException e) {
            throw new TemplateNotFoundException("Unable to get template.", e.getCause());
        }
    }

    private JasperReport loadTemplateReport(String jasperFile) throws TemplateNotFoundException {

        logger.debug("loading jasper file {}", jasperFile);

        JasperReport jasperReport;
        URL jasperFileUrl = null;
        try {
            jasperFileUrl = PrinterUtil.completeUrl(jasperFile, jasperBaseUrl);
            if (!Files.exists(Path.of(jasperFileUrl.toURI()))) {
                logger.info("Unable to find JASPER file with the given URL {}", jasperFileUrl);
                URL jrxmlFileUrl = null;
                try {
                    jrxmlFileUrl = PrinterUtil.completeUrl(jasperFile.replace(".jasper", ".jrxml"), jrxmlBaseUrl);
                    logger.info("Try to compile from JRXML with URL {}", jrxmlFileUrl);
                    File f;
                    try {
                        f = PrinterUtil.getFile(jasperFileUrl);
                    } catch (IOException e) {
                        String err = String.format("Unable to create JASPER file with the given URL %s.", jasperFileUrl.toString());
                        logger.error(err, e);
                        throw new TemplateNotFoundException(err, e);
                    }
                    try (InputStream in = jrxmlFileUrl.openStream();
                         FileOutputStream fos = new FileOutputStream(f)) {
                        JasperCompileManager.compileReportToStream(in, fos);
                    } catch (IOException | JRException e) {
                        String err = String.format("Unable to find or compile JRXML with the given URL %s.", jrxmlFileUrl.toString());
                        logger.error(err, e);
                        throw new TemplateNotFoundException(err, e);
                    }
                } catch (MalformedURLException ee) {
                    throw new TemplateNotFoundException("Bad provided URL format.", ee);
                }
            }
            //load report
            jasperReport = (JasperReport) JRLoader.loadObject(jasperFileUrl);
            logger.info("JASPER file loaded.");
        } catch (MalformedURLException | URISyntaxException e) {
            throw new TemplateNotFoundException("Bad provided URL format.", e);
        } catch (JRException e) {
            String err = String.format("Unable to load JASPER with the given URL %s.", jasperFileUrl.toString());
            logger.error(err, e);
            throw new TemplateNotFoundException(err, e);
        }

        //load recursively subreports if any
        JRElementsVisitor.visitReport(jasperReport, new JRVisitorSupport() {

            @Override
            public void visitSubreport(JRSubreport subreport) {
                String subReportFileName = subreport.getExpression().getText().replaceAll(PrinterUtil.QUOTE, PrinterUtil.EMPTY);
                String templateFolderPrefix = jasperFile.substring(0, jasperFile.lastIndexOf(PrinterUtil.SLASH) + 1);
                loadTemplateReport(templateFolderPrefix + subReportFileName);
            }
        });


        return jasperReport;
    }


}
