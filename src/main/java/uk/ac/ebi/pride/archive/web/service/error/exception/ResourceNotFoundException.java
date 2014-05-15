package uk.ac.ebi.pride.archive.web.service.error.exception;

/**
 * Exception thrown if a requested resource was not found.
 *
 * @author Florian Reisinger
 * @since 1.0.4
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
