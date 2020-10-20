package fr.pcscol.printer.service.xdoc;

public interface XdocTextStylingFieldMetadata extends XdocFieldMetadata {

    Boolean isSyntaxWithDirective();

    enum SyntaxKindEnum {
        NOESCAPE("NoEscape"),

        HTML("Html"),

        GWIKI("GWiki"),

        MEDIAWIKI("MediaWiki"),

        MARKDOWN("MarkDown");

        private String value;

        SyntaxKindEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static SyntaxKindEnum fromValue(String text) {
            for (SyntaxKindEnum b : SyntaxKindEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    SyntaxKindEnum getSyntaxKind();
}
