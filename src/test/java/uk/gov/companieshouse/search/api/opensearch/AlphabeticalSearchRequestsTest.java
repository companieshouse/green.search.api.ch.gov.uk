package uk.gov.companieshouse.search.api.opensearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.TotalHitsRelation;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.search.api.service.rest.AlphabeticalSearchRestClientService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlphabeticalSearchRequestsTest {

    @InjectMocks
    AlphabeticalSearchRequests alphabeticalSearchRequests;

    @Mock
    private AlphabeticalSearchQueries mockAlphabeticalSearchQueries;

    @Mock
    private AlphabeticalSearchRestClientService mockSearchRestClient;

    @Mock
    private EnvironmentReader mockEnvironmentReader;

    private static final String ENV_READER_RESULT = "1";
    private static final String REQUEST_ID = "requestId";
    private static final Integer SIZE = 10;

    @BeforeEach
    void setUp() throws Exception {
        when(mockEnvironmentReader.getMandatoryString(anyString()))
                .thenReturn(ENV_READER_RESULT);
        when(mockSearchRestClient.search(any(SearchRequest.class)))
                .thenReturn(createSearchResponse());
    }

    @Test
    @DisplayName("Get best match response")
    void getBestMatchResponse() throws Exception {

        HitsMetadata<Object> searchHits = alphabeticalSearchRequests
                .getBestMatchResponse("orderedAlpha", REQUEST_ID);

        assert searchHits.total() != null;
        assertEquals(1, searchHits.total().value());
    }

    @Test
    @DisplayName("Get starts with response")
    void getStartsWithResponse() throws Exception {

        HitsMetadata<Object> searchHits = alphabeticalSearchRequests
                .getStartsWithResponse("orderedAlpha", REQUEST_ID);

        assert searchHits.total() != null;
        assertEquals(1, searchHits.total().value());
    }

    @Test
    @DisplayName("Get corporate name starts with response")
    void getCorporateNameStartsWithResponse() throws Exception {

        HitsMetadata<Object> searchHits = alphabeticalSearchRequests
                .getCorporateNameStartsWithResponse("orderedAlpha", REQUEST_ID);

        assert searchHits.total() != null;
        assertEquals(1, searchHits.total().value());
    }

    @Test
    @DisplayName("Get above results response")
    void getAboveResultsResponse() throws Exception {

        HitsMetadata<Object> searchHits = alphabeticalSearchRequests
                .getAboveResultsResponse(REQUEST_ID, "orderedAlpha", "TEST", SIZE);

        assert searchHits.total() != null;
        assertEquals(1, searchHits.total().value());
    }

    @Test
    @DisplayName("Get descending results response")
    void getDescendingResultsResponse() throws Exception {

        HitsMetadata<Object> searchHits = alphabeticalSearchRequests
                .getDescendingResultsResponse(REQUEST_ID, "orderedAlpha", "TEST", SIZE);

        assert searchHits.total() != null;
        assertEquals(1, searchHits.total().value());
    }

    private SearchResponse<Object> createSearchResponse() throws Exception {

        Map<String, Object> source = new ObjectMapper().readValue(
                "{\"test\":\"value\"}",
                new TypeReference<>() {
                }
        );

        Hit<Object> hit = Hit.of(builder -> builder
                .id("1")
                .source(source));

        return SearchResponse.searchResponseOf(builder -> builder
                .took(8)
                .timedOut(false)
                .shards(s -> s
                        .total(1)
                        .successful(1)
                        .failed(0))
                .hits(h -> h
                        .hits(List.of(hit))
                        .total(t -> t
                                .value(1)
                                .relation(TotalHitsRelation.Gte))));
    }

}
