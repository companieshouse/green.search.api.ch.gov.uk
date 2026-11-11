package uk.gov.companieshouse.search.api.exception;

/**
 * Thrown when the alphakey service cannot be called successfully. Unchecked so that it is
 * translated by {@link GlobalExceptionHandler}.
 */
public class AlphaKeyServiceException extends RuntimeException {

    public AlphaKeyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
