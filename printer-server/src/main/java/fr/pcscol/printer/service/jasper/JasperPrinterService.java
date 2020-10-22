package fr.pcscol.printer.service.jasper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.UncheckedExecutionException;
import fr.opensagres.xdocreport.core.io.IOUtils;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.export.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class JasperPrinterService {

    private Logger logger = LoggerFactory.getLogger(JasperPrinterService.class);

    @Autowired
    private JasperLoaderService loaderService;

    private Cache<String, JasperPrintReport> reportCache = CacheBuilder.newBuilder().build();

    public static final String PRINTING = "PRINTING";

    @PostConstruct
    public void init() throws IOException {
        loaderService.load();
    }

    public void generate(String reportName, JsonNode data, Map<String, Object> parameters, JasperExportType exportType, OutputStream outputStream) throws TemplateNotFoundException, DocumentGenerationException {

        logger.debug("New document generation is requested with template={}, data={}, exportType={}", reportName, data, exportType);

        //create dataSource
        try (InputStream jsonStream = IOUtils.toInputStream(data.toString())) {
            JsonDataSource dataSource = new JsonDataSource(jsonStream, ".");
            //retrieve report
            JasperPrintReport templateReport = getTemplateReport(reportName);
            //print
            Map<String, Object> reportParams = new HashMap<>();
            if (parameters != null && !parameters.isEmpty()) {
                reportParams.putAll(parameters);
            }
            reportParams.put(JRParameter.REPORT_CONTEXT, templateReport.getContext());
            JasperPrint jasperPrint = JRFiller.fill(DefaultJasperReportsContext.getInstance(), templateReport.getSource(), reportParams, dataSource);
            //export
            JRAbstractExporter exporter = getExporter(exportType, parameters);
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            logger.debug("New document generated with template {}", reportName);

        } catch (TemplateNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("An error occured during document generation.", e);
            throw new DocumentGenerationException("An error occured during document generation", e);
        }
    }

    private JRAbstractExporter getExporter(JasperExportType exportType, Map<String, Object> parameters) {
        JRAbstractExporter exporter;
        switch (exportType) {
            case PDF:
                exporter = new JRPdfExporter();
                //report config
                SimplePdfReportConfiguration reportConfig
                        = new SimplePdfReportConfiguration();
                reportConfig.setSizePageToContent(true);
                reportConfig.setForceLineBreakPolicy(false);
                exporter.setConfiguration(reportConfig);
                //export config
                SimplePdfExporterConfiguration pdfExporterConfiguration
                        = new SimplePdfExporterConfiguration();
                pdfExporterConfiguration.setAllowedPermissionsHint(JasperExporterConfigParams.ALLOWED_PERMISSIONS_HINT.getString(parameters));
                pdfExporterConfiguration.setDeniedPermissionsHint(JasperExporterConfigParams.DENIED_PERMISSIONS_HINT.getString(parameters));
                pdfExporterConfiguration.setMetadataAuthor(JasperExporterConfigParams.METADATA_AUTHOR.getString(parameters));
                pdfExporterConfiguration.setMetadataCreator(JasperExporterConfigParams.METADATA_CREATOR.getString(parameters));
                pdfExporterConfiguration.setMetadataSubject(JasperExporterConfigParams.METADATA_SUBJECT.getString(parameters));
                pdfExporterConfiguration.setMetadataKeywords(JasperExporterConfigParams.METADATA_KEYWORDS.getString(parameters));
                pdfExporterConfiguration.setMetadataTitle(JasperExporterConfigParams.METADATA_TITLE.getString(parameters));
                pdfExporterConfiguration.setDisplayMetadataTitle(JasperExporterConfigParams.METADATA_DISPLAY_TITLE.getBoolean(parameters));
                exporter.setConfiguration(pdfExporterConfiguration);
                break;
            case DOCX:
                exporter = new JRDocxExporter();
                //export config
                SimpleDocxExporterConfiguration docxExporterConfiguration
                        = new SimpleDocxExporterConfiguration();
                docxExporterConfiguration.setMetadataAuthor(JasperExporterConfigParams.METADATA_AUTHOR.getString(parameters));
                docxExporterConfiguration.setMetadataSubject(JasperExporterConfigParams.METADATA_SUBJECT.getString(parameters));
                docxExporterConfiguration.setMetadataKeywords(JasperExporterConfigParams.METADATA_KEYWORDS.getString(parameters));
                docxExporterConfiguration.setMetadataTitle(JasperExporterConfigParams.METADATA_TITLE.getString(parameters));
                docxExporterConfiguration.setMetadataApplication(JasperExporterConfigParams.METADATA_APPLICATION.getString(parameters));
                docxExporterConfiguration.setEmbedFonts(JasperExporterConfigParams.EMBED_FONTS.getBoolean(parameters));
                exporter.setConfiguration(docxExporterConfiguration);
            case ODT:
                exporter = new JROdtExporter();
                break;
            default:
                throw new UnsupportedOperationException("exportType is not supported");
        }
        return exporter;
    }

    private JasperPrintReport getTemplateReport(String name) {
        try {
            return reportCache.get(name, () -> loaderService.get(name));
        } catch (ExecutionException | UncheckedExecutionException e) {
            throw new TemplateNotFoundException("Unable to get template.", e.getCause());
        }
    }


}
