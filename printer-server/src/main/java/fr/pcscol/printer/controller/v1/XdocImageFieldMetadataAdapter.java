package fr.pcscol.printer.controller.v1;

import fr.pcscol.printer.api.model.ImageFieldMetadata;
import fr.pcscol.printer.api.model.TextStylingFieldMetadata;
import fr.pcscol.printer.service.xdoc.XdocImageFieldMetadata;

public class XdocImageFieldMetadataAdapter extends XdocFieldMetadataAdapter implements XdocImageFieldMetadata {

    private final ImageFieldMetadata imageFieldMetadata;

    public XdocImageFieldMetadataAdapter(ImageFieldMetadata imageFieldMetadata){
        super(imageFieldMetadata);
        this.imageFieldMetadata = imageFieldMetadata;
    }

    @Override
    public Boolean isUseImageSize() {
        return imageFieldMetadata.isUseImageSize();
    }

    @Override
    public NullImageBehaviourEnum getNullImageBehaviour() {
        return NullImageBehaviourEnum.fromValue(imageFieldMetadata.getNullImageBehaviour().toString());
    }
}
