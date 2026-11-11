package uk.gov.companieshouse.search.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opensearch.client.transport.TransportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request =
            new MockHttpServletRequest("GET", "/alphabetical-search/companies");

    @Test
    void openSearchFailureReturnsInternalServerError() {
        request.addHeader("X-Request-ID", "abc");

        ResponseEntity<Void> response =
                handler.handleOpenSearchFailure(new TransportException("error"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void configurationFailureReturnsInternalServerErrorWithoutRequestId() {
        ResponseEntity<Void> response = handler.handleConfigurationFailure(
                new EnvironmentVariableException("missing error"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void alphaKeyFailureReturnsBadGateway() {
        ResponseEntity<Void> response = handler.handleAlphaKeyFailure(
                new AlphaKeyServiceException("down", new RuntimeException()), request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    }
}
