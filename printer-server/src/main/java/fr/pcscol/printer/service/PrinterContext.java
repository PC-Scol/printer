package fr.pcscol.printer.service;

import fr.opensagres.xdocreport.document.images.ByteArrayImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.formatter.FieldMetadata;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;
import fr.pcscol.printer.service.exception.DocumentGenerationException;

import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PrinterContext implements IContext {

    public static final long serialVersionUID = 1L;

    private final Map<String, Object> map;

    public PrinterContext() {
        this(new HashMap<String, Object>());
    }

    public PrinterContext(Map<String, Object> contextMap) {
        map = contextMap;
    }

    @Override
    public Object put(String key, Object value) {
        int index = key.indexOf('.');
        if (index != -1) {
            String[] keys = key.split("[.]");
            Map<String, Object> subMap = map;
            for (int i = 0; i < keys.length - 1; i++) {
                Object o = subMap.get(keys[i]);
                if (o == null) {
                    o = new HashMap<String, Object>();
                    subMap.put(keys[i], o);
                }
                if (o instanceof Map) {
                    subMap = (Map<String, Object>) o;
                } else {
                    throw new IllegalArgumentException("Cannot put an entry inside a non Map value.");
                }
            }
            return subMap.put(keys[keys.length - 1], value);
        }
        return map.put(key, value);
    }

    @Override
    public Object get(String key) {
        int index = key.indexOf('.');
        if (index != -1) {
            String[] keys = key.split("[.]");
            Map<String, Object> subMap = map;
            for (int i = 0; i < keys.length - 1; i++) {
                Object o = subMap.get(keys[i]);
                if (o == null) {
                    return null;
                } else {
                    if (o instanceof Map) {
                        subMap = (Map<String, Object>) o;
                    } else {
                        return null;
                    }
                }
            }
            return subMap.get(keys[keys.length - 1]);
        }
        return map.get(key);
    }

    @Override
    public void putMap(Map<String, Object> contextMap) {
        map.putAll(contextMap);
    }

    @Override
    public Map<String, Object> getContextMap() {
        return map;
    }

    public void setupImages(FieldsMetadata fieldsMetadata) throws DocumentGenerationException {
        Collection<FieldMetadata> fields = fieldsMetadata.getFieldsAsImage();
        for (FieldMetadata f : fields) {
            Object o = this.get(f.getFieldName());
            if(o instanceof IImageProvider){
                //image already setup
            } else if (o instanceof String) {
                //base64 encoded image
                ByteArrayImageProvider imageProvider = new ByteArrayImageProvider(Base64.getDecoder().decode((String) o));
                this.put(f.getFieldName(), imageProvider);
            } else if(NullImageBehaviour.ThrowsError == f.getBehaviour()){
                //throw exception earlier
                throw new DocumentGenerationException(String.format("Image '%s' is expected to be a Base64 encoded String.", f.getFieldName()));
            }
        }
    }
}
