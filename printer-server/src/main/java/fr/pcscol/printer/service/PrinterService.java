package fr.pcscol.printer.service;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.pcscol.printer.PrinterUtil;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import fr.pcscol.printer.service.exception.TemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

/**
 * The PrinterService role is to generate documents by merging a template (odt, docx) with a data model.
 * It uses the XDocReport library to make that.
 */
@Service
public class PrinterService {

    private Logger logger = LoggerFactory.getLogger(PrinterService.class);

    /**
     * report/template registry used to load and cache reports
     */
    private static XDocReportRegistry reportRegistry = XDocReportRegistry.getRegistry();

    /**
     * stop watch for generation time calculations
     */
    private StopWatch stopWatch = new StopWatch();

    /**
     * Generates a document by merging the template (odt, docx) referenced by templateUrl with the provided data.
     * The resulting document may be converted to pdf if convert is set to <code>true</code> and then written to the outputStream.
     * @param templateUrl the url of the template (odt, docx) to use
     * @param data the model to merge within the template
     * @param convert is <code>true</code> if the resulting document need to be converted to pdf
     * @param outputStream the stream used to write the document
     * @throws TemplateNotFoundException if the provided url does not reference a valid template
     * @throws DocumentGenerationException if any error occurs during the document generation
     */
    public void generate(URL templateUrl, Map<String, Object> data, boolean convert, OutputStream outputStream) throws TemplateNotFoundException, DocumentGenerationException {
        try {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            stopWatch.start();

            logger.debug("New document generation is requested with template={}, data={}, convert={}", templateUrl, data, convert);
            IXDocReport templateReport = getTemplateReport(templateUrl);
            try {
                IContext iContext = templateReport.createContext(data);
                if (!convert) {
                    templateReport.process(iContext, outputStream);
                } else {
                    templateReport.convert(iContext, Options.getFrom(getDocumentKind(templateUrl.getFile())).to(ConverterTypeTo.PDF), outputStream);
                }
                logger.debug("New document generated with template {}", templateUrl);
            } catch (XDocReportException | IOException e) {
                logger.error("An error occured during document generation.", e);
                throw new DocumentGenerationException("An error occured during document generation", e);
            }
        }finally {
            stopWatch.stop();
            long lastExecTime = stopWatch.getLastTaskTimeNanos();
            long avgExecTime = stopWatch.getTotalTimeNanos() / stopWatch.getTaskCount();
            logger.debug("Document generation took {} ns, Average generation time is {} ns.", lastExecTime, avgExecTime);
        }

    }

    /**
     * Gets the document kind of the provided file name.
     * @param fileName the file name
     * @return the {@link DocumentKind}
     * @throws DocumentGenerationException if cannot retrieve the document kind
     */
    private DocumentKind getDocumentKind(String fileName) throws DocumentGenerationException {
        String mimeType = PrinterUtil.getMimeType(fileName);
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


    /**
     * Gets/Loads the report/template with a given templateUrl.
     * @param templateUrl the template url
     * @return the report/template
     * @throws TemplateNotFoundException if the template cannot be found or loaded
     */
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
