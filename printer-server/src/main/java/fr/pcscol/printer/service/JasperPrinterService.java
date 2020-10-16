package fr.pcscol.printer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import fr.opensagres.xdocreport.core.io.IOUtils;
import fr.pcscol.printer.PrinterUtil;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.engine.fill.JasperReportSource;
import net.sf.jasperreports.engine.fill.SimpleJasperReportSource;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRVisitorSupport;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.repo.JasperDesignCache;
import net.sf.jasperreports.repo.SimpleRepositoryResourceContext;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class JasperPrinterService {

    @Value("${printer.jasper.base-path}")
    private String jasperBasePath;

    private Cache<String, Pair<JasperReport, ReportContext>> reportCache = CacheBuilder.newBuilder().build();

    @Autowired
    private ObjectMapper objectMapper;


    public static final String PRINTING = "PRINTING";
    private Logger logger = LoggerFactory.getLogger(JasperPrinterService.class);

    public void generate(String jasperFile, Object data, JasperExportType exportType, OutputStream outputStream) throws TemplateNotFoundException, DocumentGenerationException {

        logger.debug("New document generation is requested with template={}, data={}, exportType={}", jasperFile, data, exportType);

        try {
            //create dataSource
            JsonNode jsonNode = objectMapper.convertValue(data, JsonNode.class);
            JsonDataSource dataSource = new JsonDataSource(IOUtils.toInputStream(jsonNode.toString()), ".");
            //retrieve report
            Pair<JasperReport, ReportContext> templateReport = getTemplateReport(jasperFile);
            //print
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(JRParameter.REPORT_CONTEXT, templateReport.getRight());
            String reportLocation = Path.of(jasperBasePath, jasperFile).getParent().toString();
            JasperReportSource reportSource = SimpleJasperReportSource.from(templateReport.getLeft(), reportLocation, SimpleRepositoryResourceContext.of(reportLocation));
            JasperPrint jasperPrint = JRFiller.fill(DefaultJasperReportsContext.getInstance(), reportSource, parameters, dataSource);
            //export
            JRAbstractExporter exporter = getExporter(exportType);
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            logger.debug("New document generated with template {}", jasperFile);
        } catch (TemplateNotFoundException e) {
            throw e;
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

    private Pair<JasperReport, ReportContext> getTemplateReport(String reportFile) {
        try {
            return reportCache.get(reportFile, () -> {
                Path reportFilePath = Path.of(jasperBasePath, reportFile);
                ReportContext reportContext = new SimpleReportContext();
                JasperReport report = loadTemplateReport(reportFilePath, reportContext);
                return Pair.of(report, reportContext);
            });
        } catch (ExecutionException | UncheckedExecutionException e) {
            throw new TemplateNotFoundException("Unable to get template.", e.getCause());
        }
    }

    private JasperReport loadTemplateReport(Path reportFilePath, ReportContext reportContext) throws TemplateNotFoundException, IOException {

        logger.debug("loading jasper file {}", reportFilePath);

        JasperReport jasperReport;
        File reportFile = new File(reportFilePath.toUri());

        if (!reportFile.exists()) {
            logger.info("Unable to find JASPER file @ {}", reportFilePath);
            Path jrxmlFilePath = Path.of(reportFilePath.toString().replace(".jasper", ".jrxml"));
            File jrxmlFile = new File(jrxmlFilePath.toUri());
            if (jrxmlFile.exists()) {
                logger.info("Try to compile from JRXML @ {}", jrxmlFilePath);
                //create jasper file
                reportFile.createNewFile();
                reportFile.deleteOnExit();

                try (InputStream in = new FileInputStream(jrxmlFile);
                     FileOutputStream fos = new FileOutputStream(reportFile)) {
                    JasperCompileManager.compileReportToStream(in, fos);
                } catch (JRException e) {
                    String err = String.format("Unable to compile JRXML @ %s.", jrxmlFilePath.toString());
                    logger.error(err, e);
                    throw new TemplateNotFoundException(err, e);
                }
            } else {
                String err = String.format("Unable to find JRXML @ %s.", jrxmlFilePath.toString());
                logger.error(err);
                throw new TemplateNotFoundException(err);
            }
        }
        //jasper file is here => load it
        try {
            jasperReport = (JasperReport) JRLoader.loadObject(reportFile);
            logger.info("JASPER file @ {} was loaded.", reportFilePath);
        } catch (JRException e) {
            String err = String.format("Unable to load JASPER file @ %s.", reportFilePath);
            logger.error(err, e);
            throw new TemplateNotFoundException(err, e);
        }

        //load recursively subreports if any
        JRElementsVisitor.visitReport(jasperReport, new JRVisitorSupport() {

            @Override
            public void visitSubreport(JRSubreport subreport) {
                String subReportFileName = subreport.getExpression().getText().replaceAll(PrinterUtil.QUOTE, PrinterUtil.EMPTY);
                Path subReportFilePath = Path.of(reportFilePath.getParent().toString(), subReportFileName);
                try {
                    JasperReport subReport = loadTemplateReport(subReportFilePath, reportContext);
                    //cache it
                    JasperDesignCache cache = JasperDesignCache.getInstance(DefaultJasperReportsContext.getInstance(), reportContext);
                    cache.set(subReportFileName, subReport);
                } catch (IOException e) {
                    String err = String.format("Unable to compile JRXML @ %s.", subReportFilePath);
                    logger.error(err, e);
                    throw new TemplateNotFoundException(err, e);
                }
            }
        });

        return jasperReport;


    }
}
