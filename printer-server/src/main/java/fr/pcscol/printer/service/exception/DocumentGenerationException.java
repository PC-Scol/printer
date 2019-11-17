package fr.pcscol.printer.service.exception;

public class DocumentGenerationException extends Exception {

    public DocumentGenerationException(String message) {
        super(message);
    }

    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
