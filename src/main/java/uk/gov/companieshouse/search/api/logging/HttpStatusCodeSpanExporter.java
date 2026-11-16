package uk.gov.companieshouse.search.api.logging;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Micrometer's OpenTelemetry bridge writes every observation key as a string attribute, so the
 * HTTP status code is exported as "404" rather than 404. Consumers such as X-Ray only read it
 * when it is numeric, so it is rewritten as a long on export.
 */
public class HttpStatusCodeSpanExporter implements SpanExporter {

    static final String STATUS_CODE_KEY = "http.response.status_code";

    private final SpanExporter delegate;

    public HttpStatusCodeSpanExporter(SpanExporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        return delegate.export(spans.stream().map(this::withNumericStatusCode).collect(Collectors.toList()));
    }

    @Override
    public CompletableResultCode flush() {
        return delegate.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return delegate.shutdown();
    }

    private SpanData withNumericStatusCode(SpanData span) {
        String status = span.getAttributes().get(AttributeKey.stringKey(STATUS_CODE_KEY));
        if (status == null) {
            return span;
        }
        long statusCode;
        try {
            statusCode = Long.parseLong(status);
        } catch (NumberFormatException e) {
            return span;
        }

        AttributesBuilder builder = Attributes.builder();
        span.getAttributes().forEach((key, value) -> {
            if (!(key.getKey().equals(STATUS_CODE_KEY) && key.getType() == AttributeType.STRING)) {
                copyAttribute(builder, key, value);
            }
        });
        builder.put(AttributeKey.longKey(STATUS_CODE_KEY), statusCode);
        Attributes attributes = builder.build();

        return new DelegatingSpanData(span) {
            @Override
            public Attributes getAttributes() {
                return attributes;
            }

            @Override
            public int getTotalAttributeCount() {
                return attributes.size();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static void copyAttribute(AttributesBuilder builder, AttributeKey<?> key, Object value) {
        builder.put((AttributeKey<Object>) key, value);
    }
}
