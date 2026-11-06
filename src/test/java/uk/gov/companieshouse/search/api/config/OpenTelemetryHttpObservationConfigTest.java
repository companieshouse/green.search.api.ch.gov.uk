package uk.gov.companieshouse.search.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationConvention;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class OpenTelemetryHttpObservationConfigTest {

    private final ServerRequestObservationConvention convention =
            new OpenTelemetryHttpObservationConfig().serverRequestObservationConvention();

    @Test
    void conventionEmitsOpenTelemetryHttpAttributes() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        ServerRequestObservationContext context = new ServerRequestObservationContext(
                new MockHttpServletRequest("GET", "/green/alphabetical-search/companies"), response);
        context.setPathPattern("/alphabetical-search/companies");

        KeyValues keyValues = convention.getLowCardinalityKeyValues(context);

        assertEquals("GET", value(keyValues, "http.request.method"));
        assertEquals("200", value(keyValues, "http.response.status_code"));
        assertEquals("/alphabetical-search/companies", value(keyValues, "http.route"));
        assertEquals("http://localhost/green/alphabetical-search/companies",
                value(convention.getHighCardinalityKeyValues(context), "url.full"));
    }

    private String value(KeyValues keyValues, String key) {
        return keyValues.stream()
                .filter(keyValue -> keyValue.getKey().equals(key))
                .map(KeyValue::getValue)
                .findFirst()
                .orElse(null);
    }
}
