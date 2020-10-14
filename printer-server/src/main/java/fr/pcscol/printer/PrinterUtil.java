package fr.pcscol.printer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

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
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";


    public static final File getFile(URL url) throws URISyntaxException, IOException {
        File f = new File(url.toURI());
        f.getParentFile().mkdirs();
        f.createNewFile();
        return f;
    }

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
}
