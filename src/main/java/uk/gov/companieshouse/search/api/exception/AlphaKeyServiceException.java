package uk.gov.companieshouse.search.api.exception;

/**
 * Thrown when the alphakey service cannot be called.
 */
public class AlphaKeyServiceException extends RuntimeException {

    public AlphaKeyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
