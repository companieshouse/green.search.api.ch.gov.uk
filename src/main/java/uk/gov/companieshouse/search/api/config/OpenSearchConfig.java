package uk.gov.companieshouse.search.api.config;

import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5Transport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.search.api.exception.EndpointException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static uk.gov.companieshouse.search.api.logging.LoggingUtils.getLogger;

@Configuration
public class OpenSearchConfig {

    private final EnvironmentReader environmentReader;

    public OpenSearchConfig(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    private static final String ALPHABETICAL_SEARCH_URL_ENV = "ALPHABETICAL_SEARCH_URL";

    // IAM action/service name prefix used by Amazon OpenSearch Service for SigV4 signing (e.g. es:ESHttpPost)
    private static final String OPENSEARCH_SIGNING_SERVICE_NAME = "es";

    // Environment variable used to switch between a local unsigned OpenSearch client and an
    // AWS SigV4-signed client.
    private static final String USE_AWS_SIGV4 = "USE_AWS_SIGV4";

    @Bean
    public OpenSearchClient alphabeticalSearchRestClient() {
        boolean useAwsSigV4 = Boolean.TRUE.equals(environmentReader.getOptionalBoolean(USE_AWS_SIGV4));

        return useAwsSigV4
                ? createSigV4OpenSearchClient()
                : createUnsignedOpenSearchClient();
    }

    private OpenSearchClient createSigV4OpenSearchClient() {
        URL endpoint = readEndpoint();

        SdkHttpClient httpClient = ApacheHttpClient.builder().build();
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().build();
        Region region = DefaultAwsRegionProviderChain.builder().build().getRegion();

        getLogger().info("Region is: " + region);

        OpenSearchTransport transport = new AwsSdk2Transport(
                httpClient,
                endpoint.getHost(),
                OPENSEARCH_SIGNING_SERVICE_NAME,
                region,
                AwsSdk2TransportOptions.builder()
                        .setMapper(new JacksonJsonpMapper())
                        .setCredentials(credentialsProvider)
                        .build()
        );

        return new OpenSearchClient(transport);
    }

    private OpenSearchClient createUnsignedOpenSearchClient() {
        URL endpoint = readEndpoint();

        HttpHost httpHost = new HttpHost(endpoint.getProtocol(), endpoint.getHost(), endpoint.getPort());

        ApacheHttpClient5Transport transport = ApacheHttpClient5TransportBuilder
                .builder(httpHost)
                .setMapper(new JacksonJsonpMapper())
                .setHttpClientConfigCallback(
                        HttpAsyncClientBuilder::disableContentCompression
                )
                .build();

        return new OpenSearchClient(transport);
    }

    private URL readEndpoint() {
        try {
            String rawUrl = environmentReader.getMandatoryString(ALPHABETICAL_SEARCH_URL_ENV);
            URI uri = new URI(rawUrl);
            return uri.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new EndpointException(
                    ALPHABETICAL_SEARCH_URL_ENV + " environment variable is malformed; expected format is <protocol>://<host>[:port]"
            );
        }
    }
}
