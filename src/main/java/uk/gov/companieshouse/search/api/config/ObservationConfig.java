package uk.gov.companieshouse.search.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.OpenTelemetryServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

@Configuration
public class ObservationConfig {

    // Emits OpenTelemetry HTTP semantic convention attributes (http.request.method,
    // http.response.status_code, http.route) instead of Micrometer's method/status/uri keys,
    // which X-Ray does not map.
    @Bean
    ServerRequestObservationConvention serverRequestObservationConvention() {
        return new OpenTelemetryServerRequestObservationConvention();
    }
}
