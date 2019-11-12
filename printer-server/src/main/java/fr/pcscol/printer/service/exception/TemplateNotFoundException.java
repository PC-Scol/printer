package fr.pcscol.printer.service.exception;

public class TemplateNotFoundException extends Exception {

    public TemplateNotFoundException(){}

    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
