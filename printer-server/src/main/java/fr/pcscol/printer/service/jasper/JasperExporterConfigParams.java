package fr.pcscol.printer.service.jasper;

import java.util.Map;
import java.util.Optional;

public enum JasperExporterConfigParams {

    ALLOWED_PERMISSIONS_HINT("exporter.allowedPermissionsHint"),
    DENIED_PERMISSIONS_HINT("exporter.deniedPermissionsHint"),
    METADATA_TITLE("exporter.metadataTitle"),
    METADATA_AUTHOR("exporter.metadataAuthor"),
    METADATA_SUBJECT("exporter.metadataSubject"),
    METADATA_KEYWORDS("exporter.metadataKeywords"),
    METADATA_CREATOR("exporter.metadataCreator"),
    METADATA_APPLICATION("exporter.metadataApplication"),
    EMBED_FONTS("exporter.embedFonts"),
    METADATA_DISPLAY_TITLE("exporter.displayMetadataTitle");

    private String value;

    JasperExporterConfigParams(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public String getString(Map<String, Object> parameters) {
        if (parameters == null) {
            return null;
        }
        Optional<String> s = Optional.ofNullable((String)parameters.get(value));
        return s.orElse(null);
    }

    public Boolean getBoolean(Map<String, Object> parameters) {
        if (parameters == null) {
            return null;
        }
        Optional<Boolean> b = Optional.ofNullable((Boolean)parameters.get(value));
        return b.orElse(null);
    }

    public static JasperExporterConfigParams fromValue(String text) {
        for (JasperExporterConfigParams b : JasperExporterConfigParams.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
