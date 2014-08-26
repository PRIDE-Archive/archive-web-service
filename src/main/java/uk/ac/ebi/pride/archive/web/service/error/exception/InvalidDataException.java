package uk.ac.ebi.pride.archive.web.service.error.exception;

/**
 * @author florian@ebi.ac.uk
 */
public class InvalidDataException extends Exception {

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDataException(Throwable cause) {
        super(cause);
    }
}
