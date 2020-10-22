package fr.pcscol.printer.service.xdoc;

public interface XdocImageFieldMetadata extends XdocFieldMetadata {

    Boolean isUseImageSize();

    enum NullImageBehaviourEnum {
        THROWSERROR("ThrowsError"),

        REMOVEIMAGETEMPLATE("RemoveImageTemplate"),

        KEEPIMAGETEMPLATE("KeepImageTemplate");

        private String value;

        NullImageBehaviourEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static NullImageBehaviourEnum fromValue(String text) {
            for (NullImageBehaviourEnum b : NullImageBehaviourEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    NullImageBehaviourEnum getNullImageBehaviour();
}
