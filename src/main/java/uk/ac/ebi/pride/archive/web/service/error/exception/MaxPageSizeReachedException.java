package uk.ac.ebi.pride.archive.web.service.error.exception;

/**
 * Exception thrown if the number of items requested exceed the page size limit
 *
 * @author ntoro@ebi.ac.uk
 */
public class MaxPageSizeReachedException extends RuntimeException {

    public MaxPageSizeReachedException(String message) {
        super(message);
    }
}
