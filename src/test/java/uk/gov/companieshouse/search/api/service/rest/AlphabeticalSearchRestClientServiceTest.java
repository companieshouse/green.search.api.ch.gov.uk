package uk.gov.companieshouse.search.api.service.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlphabeticalSearchRestClientServiceTest {

    @Mock
    private OpenSearchClient alphabeticalOpenSearchClient;

    @InjectMocks
    private AlphabeticalSearchRestClientService service;

    @Mock
    private SearchRequest searchRequest;

    @Mock
    private SearchResponse<Object> searchResponse;

    @Test
    void searchShouldDelegateToOpenSearchClient() throws IOException {

        when(alphabeticalOpenSearchClient.search(searchRequest, Object.class))
                .thenReturn(searchResponse);

        SearchResponse<Object> response = service.search(searchRequest);

        assertSame(searchResponse, response);

        verify(alphabeticalOpenSearchClient).search(searchRequest, Object.class);
    }
}
