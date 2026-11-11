package uk.gov.companieshouse.search.api.exception;

import static uk.gov.companieshouse.search.api.logging.LoggingUtils.getLogger;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.transport.TransportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.logging.util.DataMap;

/**
 * Logs failures with the request ID and returns a Http status, rather than leaving them to
 * Spring's default error handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    @ExceptionHandler(AlphaKeyServiceException.class)
    ResponseEntity<Void> handleAlphaKeyFailure(Exception e, HttpServletRequest request) {
        logFailure("Alphakey service call failed", e, request);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    @ExceptionHandler({OpenSearchException.class, TransportException.class})
    ResponseEntity<Void> handleOpenSearchFailure(Exception e, HttpServletRequest request) {
        return internalServerError("OpenSearch request failed", e, request);
    }

    @ExceptionHandler({EnvironmentVariableException.class})
    ResponseEntity<Void> handleConfigurationFailure(Exception e, HttpServletRequest request) {
        return internalServerError("Invalid or missing application configuration", e, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Void> handleUnexpectedFailure(Exception e, HttpServletRequest request) {
        return internalServerError("Unexpected error", e, request);
    }

    private ResponseEntity<Void> internalServerError(String message, Exception e,
                                                     HttpServletRequest request) {
        logFailure(message, e, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private void logFailure(String message, Exception e, HttpServletRequest request) {
        Map<String, Object> logMap = new DataMap.Builder()
                .requestId(request.getHeader(REQUEST_ID_HEADER_NAME))
                .build().getLogMap();
        logMap.put("path", request.getRequestURI());
        getLogger().error(message, e, logMap);
    }
}
