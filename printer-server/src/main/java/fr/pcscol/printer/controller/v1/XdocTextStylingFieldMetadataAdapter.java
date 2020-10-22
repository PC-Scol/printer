package fr.pcscol.printer.controller.v1;

import fr.pcscol.printer.api.model.TextStylingFieldMetadata;
import fr.pcscol.printer.service.xdoc.XdocTextStylingFieldMetadata;

public class XdocTextStylingFieldMetadataAdapter extends XdocFieldMetadataAdapter implements XdocTextStylingFieldMetadata {

    private final TextStylingFieldMetadata textStylingFieldMetadata;

    public XdocTextStylingFieldMetadataAdapter(TextStylingFieldMetadata textStylingFieldMetadata){
        super(textStylingFieldMetadata);
        this.textStylingFieldMetadata = textStylingFieldMetadata;
    }

    @Override
    public Boolean isSyntaxWithDirective() {
        return textStylingFieldMetadata.isSyntaxWithDirective();
    }

    @Override
    public SyntaxKindEnum getSyntaxKind() {
        return SyntaxKindEnum.fromValue(textStylingFieldMetadata.getSyntaxKind().toString());
    }
}
