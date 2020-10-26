package fr.pcscol.printer.service.jasper;

import java.util.Map;
import java.util.Optional;

public enum JasperExporterConfigParams {

    PDF_REPORT_FORCE_SVG_SHAPES("exporter.pdf.report.forceSvgShapes"),
    PDF_REPORT_SIZE_PAGE_TO_CONTENT("exporter.pdf.report.sizePageToContent"),
    PDF_REPORT_FORCE_LINE_BREAK_POLICY("exporter.pdf.report.forceLineBreakPolicy"),
    PDF_EXPORT_ALLOWED_PERMISSIONS_HINT("exporter.pdf.export.allowedPermissionsHint"),
    PDF_EXPORT_DENIED_PERMISSIONS_HINT("exporter.pdf.export.deniedPermissionsHint"),
    PDF_EXPORT_METADATA_CREATOR("exporter.pdf.export.metadataCreator"),
    PDF_EXPORT_METADATA_DISPLAY_TITLE("exporter.pdf.export.displayMetadataTitle"),
    ALL_EXPORT_METADATA_TITLE("exporter.all.export.metadataTitle"),
    ALL_EXPORT_METADATA_AUTHOR("exporter.all.export.metadataAuthor"),
    ALL_EXPORT_METADATA_SUBJECT("exporter.all.export.metadataSubject"),
    ALL_EXPORT_METADATA_KEYWORDS("exporter.all.export.metadataKeywords"),
    DOCX_EXPORT_METADATA_APPLICATION("exporter.docx.export.metadataApplication"),
    DOCX_EXPORT_EMBED_FONTS("exporter.docx.export.embedFonts"),
    CSV_EXPORT_WRITE_BOM("exporter.csv.export.writeBom"),
    CSV_EXPORT_FIELD_ENCLOSURE("exporter.csv.export.fieldEnclosure"),
    CSV_EXPORT_FORCE_FIELD_ENCLOSURE("exporter.csv.export.forceFieldEnclosure"),
    CSV_EXPORT_RECORD_DELIMITER("exporter.csv.export.recordDelimiter"),
    CSV_EXPORT_FIELD_DELIMITER("exporter.csv.export.fieldDelimiter");

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

    public String[] getStringArray(Map<String, Object> parameters) {
        if (parameters == null) {
            return null;
        }
        Optional<String[]> s = Optional.ofNullable((String[])parameters.get(value));
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
