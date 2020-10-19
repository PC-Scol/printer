package fr.pcscol.printer.service.jasper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
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

    @Autowired
    private ObjectMapper objectMapper;

    public static final String PRINTING = "PRINTING";

    @PostConstruct
    public void init() throws IOException {
        loaderService.load();
    }

    public void generate(String reportName, Object data, JasperExportType exportType, OutputStream outputStream) throws TemplateNotFoundException, DocumentGenerationException {

        logger.debug("New document generation is requested with template={}, data={}, exportType={}", reportName, data, exportType);

        //create dataSource
        JsonNode jsonNode = objectMapper.convertValue(data, JsonNode.class);
        try (InputStream jsonStream = IOUtils.toInputStream(jsonNode.toString())) {
            JsonDataSource dataSource = new JsonDataSource(jsonStream, ".");
            //retrieve report
            JasperPrintReport templateReport = getTemplateReport(reportName);
            //print
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(JRParameter.REPORT_CONTEXT, templateReport.getContext());
            JasperPrint jasperPrint = JRFiller.fill(DefaultJasperReportsContext.getInstance(), templateReport.getSource(), parameters, dataSource);
            //export
            JRAbstractExporter exporter = getExporter(exportType);
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

    private JRAbstractExporter getExporter(JasperExportType exportType) {
        JRAbstractExporter exporter;
        switch (exportType) {
            case PDF:
                exporter = new JRPdfExporter();
                //report config
                SimplePdfReportConfiguration reportConfig
                        = new SimplePdfReportConfiguration();
                reportConfig.setSizePageToContent(true);
                //reportConfig.setForceLineBreakPolicy(false);
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

    private JasperPrintReport getTemplateReport(String name) {
        try {
            return reportCache.get(name, () -> loaderService.get(name));
        } catch (ExecutionException | UncheckedExecutionException e) {
            throw new TemplateNotFoundException("Unable to get template.", e.getCause());
        }
    }


}
