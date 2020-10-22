package fr.pcscol.printer.controller.v2;

import fr.pcscol.printer.api.v2.model.FieldMetadata;
import fr.pcscol.printer.service.xdoc.XdocFieldMetadata;

public class XdocFieldMetadataAdapter implements XdocFieldMetadata {

    private final FieldMetadata fieldMetadata;

    public XdocFieldMetadataAdapter(FieldMetadata fieldMetadata){
        this.fieldMetadata = fieldMetadata;
    }

    @Override
    public String getFieldName() {
        return fieldMetadata.getFieldName();
    }

    @Override
    public Boolean isListType() {
        return fieldMetadata.isListType();
    }
}
