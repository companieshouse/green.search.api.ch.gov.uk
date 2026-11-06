package uk.gov.companieshouse.search.api.config;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.OpenTelemetryServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

@Configuration
public class OpenTelemetryHttpObservationConfig {

    // Emits OpenTelemetry HTTP semantic convention attributes (http.request.method,
    // http.response.status_code, http.route) instead of Micrometer's method/status/uri keys,
    // which X-Ray does not map.
    @Bean
    ServerRequestObservationConvention serverRequestObservationConvention() {
        return new UrlAwareConvention();
    }

    // The standard convention only emits url.path and url.scheme, so X-Ray cannot build the URL.
    static class UrlAwareConvention extends OpenTelemetryServerRequestObservationConvention {

        @Override
        public KeyValues getHighCardinalityKeyValues(ServerRequestObservationContext context) {
            HttpServletRequest request = context.getCarrier();
            KeyValues keyValues = super.getHighCardinalityKeyValues(context);
            StringBuffer url = request.getRequestURL();
            return url == null ? keyValues : keyValues.and(KeyValue.of("url.full", url.toString()));
        }
    }
}
