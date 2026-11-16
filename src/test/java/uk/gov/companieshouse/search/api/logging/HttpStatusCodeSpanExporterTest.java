package uk.gov.companieshouse.search.api.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

class HttpStatusCodeSpanExporterTest {

    private final List<SpanData> original = new ArrayList<>();
    private final List<SpanData> exported = new ArrayList<>();

    private final SpanExporter delegate = new RecordingExporter(exported);
    private final HttpStatusCodeSpanExporter exporter = new HttpStatusCodeSpanExporter(delegate);

    private final SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(new RecordingExporter(original)))
            .build();

    @Test
    void convertsStringStatusCodeToLongAndPreservesOtherAttributes() {
        Span span = tracerProvider.get("test").spanBuilder("GET /search").startSpan();
        span.setAttribute("http.response.status_code", "400");
        span.setAttribute("http.request.method", "GET");
        span.setAttribute("retries", 2L);
        span.end();

        exporter.export(original);

        Attributes attributes = exported.getFirst().getAttributes();
        assertEquals(400L, attributes.get(AttributeKey.longKey("http.response.status_code")));
        assertNull(attributes.get(AttributeKey.stringKey("http.response.status_code")));
        assertEquals("GET", attributes.get(AttributeKey.stringKey("http.request.method")));
        assertEquals(2L, attributes.get(AttributeKey.longKey("retries")));
        assertEquals(3, attributes.size());
        assertEquals("GET /search", exported.getFirst().getName());
    }

    @Test
    void spanWithoutStatusCodeIsPassedThrough() {
        Span span = tracerProvider.get("test").spanBuilder("GET /search").startSpan();
        span.setAttribute("http.request.method", "GET");
        span.end();

        exporter.export(original);

        assertSame(original.getFirst(), exported.getFirst());
    }

    private record RecordingExporter(List<SpanData> target) implements SpanExporter {

        @Override
        public CompletableResultCode export(Collection<SpanData> spans) {
            target.addAll(spans);
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }
}
