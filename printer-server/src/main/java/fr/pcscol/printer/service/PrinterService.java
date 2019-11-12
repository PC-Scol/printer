package fr.pcscol.printer.service;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class PrinterService {

    private Logger logger = LoggerFactory.getLogger(PrinterService.class);

    private static XDocReportRegistry reportRegistry = XDocReportRegistry.getRegistry();

    public void generate(URL templateUrl, Map<String, Object> data, boolean convert, OutputStream outputStream) throws TemplateNotFoundException, DocumentGenerationException {
        logger.debug("New document generation is requested with template={}, data={}, convert={}", templateUrl, data, convert);
        IXDocReport templateReport = getTemplateReport(templateUrl);
        try {
            IContext iContext = templateReport.createContext(data);
            if(!convert){
                templateReport.process(iContext, outputStream);
            }else{
                templateReport.convert(iContext, Options.getFrom(getDocumentKind(templateUrl.getFile())).to(ConverterTypeTo.PDF), outputStream);
            }
            logger.debug("New document generated with template {}", templateUrl);
        } catch (XDocReportException | IOException e) {
            logger.error("An error occured during document generation.", e);
            throw new DocumentGenerationException("An error occured during document generation", e);
        }

    }

    private DocumentKind getDocumentKind(String fileName) throws DocumentGenerationException {
        String mimeType;
        try {
            mimeType = Files.probeContentType(Path.of(fileName));
        } catch (IOException e) {
            throw new DocumentGenerationException(String.format("Unable to detect the provided template contentType for %s", fileName));
        }
        if(mimeType == null){
            throw new DocumentGenerationException(String.format("Unable to detect the provided template contentType for %s", fileName));
        }else{
            DocumentKind documentKind = DocumentKind.fromMimeType(mimeType);
            if(documentKind == null){
                throw new DocumentGenerationException(String.format("The provided template contentType %s cannot be used for pdf conversion", mimeType));
            }
            return documentKind;
        }
    }


    private IXDocReport getTemplateReport(URL templateUrl) throws TemplateNotFoundException {

        String reportId = String.valueOf(templateUrl.hashCode());

        if (reportRegistry.existsReport(reportId)) {
            //return cached report
            logger.debug("Get report with id={} from cache", reportId);
            return reportRegistry.getReport(reportId);
        } else {
            //load new report
            try (InputStream in = templateUrl.openStream()) {
                logger.debug("Loading new report with id={} and put in cache", reportId);
                return reportRegistry.loadReport(in, reportId, TemplateEngineKind.Freemarker, true);
            } catch (IOException e) {
                logger.error("Unable to find template with the given URL {}", templateUrl, e);
                throw new TemplateNotFoundException(String.format("Unable to find template with the given URL %s.", templateUrl.toString()), e);
            } catch (XDocReportException e){
                logger.error("Unable to load template with the given URL {}", templateUrl, e);
                throw new TemplateNotFoundException(String.format("Unable to load template with the given URL %s.", templateUrl.toString()), e);
            }

        }
    }

}
