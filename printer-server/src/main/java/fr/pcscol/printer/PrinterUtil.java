package fr.pcscol.printer;

import fr.pcscol.printer.service.jasper.JasperExportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class PrinterUtil {

    private static Logger logger = LoggerFactory.getLogger(PrinterUtil.class);

    public static final HashMap<String, String> mimeMap;

    static {
        mimeMap = new HashMap<>();
        mimeMap.put("pdf", "application/pdf");
        mimeMap.put("odt", "application/vnd.oasis.opendocument.text");
        mimeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeMap.put("doc", "application/msword");
    }

    public static final String SLASH = "/";
    public static final String EMPTY = "";
    public static final String QUOTE = "\"";
    public static final String DOT = ".";
    public static final String UNDERSCORE = "_";

    public static final URL completeUrl(final String templateUrl, final String templateBaseUrl) throws MalformedURLException {
        URI uri;
        try {
            uri = new URI(templateUrl);
        } catch (URISyntaxException e) {
            throw new MalformedURLException(String.format("Provided URL format is not correct : %s", templateUrl));
        }
        if (uri.isAbsolute()) {
            return new URL(templateUrl);
        } else {
            String temp = templateUrl;
            String sep = templateBaseUrl.endsWith(SLASH) ? EMPTY : SLASH;
            if (templateUrl.startsWith(SLASH)) {
                temp = templateUrl.substring(1);
            }
            return new URL(templateBaseUrl.concat(sep).concat(temp));
        }
    }

    public static final String getMimeType(String fileName) {
        /**
         *  Files.probeContentType method uses mapping files not present on distroless/java image.
         *  So we need to resolve the mimeType manually
         */
        String ext = fileName.substring(fileName.lastIndexOf(DOT) + 1);
        return mimeMap.get(ext);
    }

    public static final String getMimeType(JasperExportType exportType) {
        /**
         *  Files.probeContentType method uses mapping files not present on distroless/java image.
         *  So we need to resolve the mimeType manually
         */
        String ext = exportType.toString().toLowerCase();
        return mimeMap.get(ext);
    }

    public static String extractOutputFileName(String originalPath, String suffix, boolean convert) {
        String fileName;
        String ext;
        int slashIndex = originalPath.lastIndexOf(SLASH);
        int dotIndex = originalPath.lastIndexOf(DOT);

        if (dotIndex == -1) {
            throw new IllegalArgumentException("Cannot retrieve file extension");
        }
        ext = originalPath.substring(dotIndex + 1);
        if (slashIndex == -1) {
            fileName = originalPath.substring(0, dotIndex);
        } else {
            fileName = originalPath.substring(slashIndex + 1, dotIndex);
        }

        StringBuilder sb = new StringBuilder().append(fileName);
        if (!StringUtils.isEmpty(suffix)) {
            sb.append(UNDERSCORE).append(suffix);
        }
        return sb.append(DOT).append(convert == true ? "pdf" : ext).toString();
    }

    public static void unzipTemplate(URL templateUrl, Path destination) throws IOException {

        try (InputStream inputStream = templateUrl.openStream();
             ZipInputStream zis = new ZipInputStream(inputStream)) {

            File parent = new File(destination.toUri());
            if(!parent.exists()){
                parent.mkdirs();
            }

            ZipEntry zipEntry = zis.getNextEntry();
            byte[] buffer = new byte[1024];
            while (zipEntry != null) {

                File newFile = new File(Path.of(parent.getPath(), zipEntry.getName()).toUri());
                newFile.deleteOnExit();
                logger.debug("Unzip file : {}", newFile.getAbsoluteFile());
                if (zipEntry.isDirectory()) {
                    newFile.mkdir();
                } else {
                    newFile.createNewFile();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
                logger.debug("Unzipped file : {}", newFile.getAbsoluteFile());
            }
            zis.closeEntry();
        } catch (IOException e) {
            String err = String.format("Unable to load template file @ %s.", templateUrl);
            throw e;
        }
    }
}
