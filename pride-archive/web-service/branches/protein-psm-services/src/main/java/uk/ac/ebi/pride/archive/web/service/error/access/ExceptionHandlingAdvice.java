package uk.ac.ebi.pride.archive.web.service.error.access;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.social.InternalServerErrorException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.pride.archive.web.service.error.exception.ResourceNotFoundException;
import uk.ac.ebi.pride.web.util.exception.RestError;
import uk.ac.ebi.pride.web.util.exception.RestErrorRegistry;

import java.security.Principal;

/**
 * @author Jose A. Dianes
 * @author Rui Wang
 * @author Florian Reisinger
 * @since 1.0.1
 */
@ControllerAdvice
public class ExceptionHandlingAdvice {

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AccessDeniedException.class)
    public
    @ResponseBody
    RestError handleAccessDeniedException(AccessDeniedException ex, Principal principal) {
        RestError accessDeny = RestErrorRegistry.getRestErrorByClass(AccessDeniedException.class);

        if (principal != null) {
            accessDeny.setDeveloperMessage("Access denied for user " + principal.getName());
        } else {
            accessDeny.setDeveloperMessage("Access denied for unknown user details.");
        }

        return accessDeny;
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public
    @ResponseBody
    RestError handleResourceNotFoundException(ResourceNotFoundException ex) {
        // ToDo: perhaps add to RestErrorRegistry in the web utils package
        return new RestError(HttpStatus.NOT_FOUND, 99999, ex.getMessage(), null);
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalServerErrorException.class)
    public
    @ResponseBody
    RestError handleInternalServerErrorException(InternalServerErrorException ex) {
        // ToDo: perhaps add to RestErrorRegistry in the web utils package
        return new RestError(HttpStatus.INTERNAL_SERVER_ERROR, 99998, ex.getMessage(), null);
    }
}
