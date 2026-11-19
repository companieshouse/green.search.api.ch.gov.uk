package uk.gov.companieshouse.search.api.opensearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.core.SearchRequest;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;
import uk.gov.companieshouse.search.api.service.rest.AdvancedSearchRestClientService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@ExtendWith(MockitoExtension.class)
class AdvancedSearchRequestsTest {

    @Mock
    private AdvancedSearchRestClientService restClientService;

    @Mock
    private AdvancedSearchQueries queries;

    @Mock
    private ConfiguredIndexNamesProvider indices;

    @Test
    void buildsSearchRequestAgainstAdvancedIndex() throws Exception {
        AdvancedSearchQueryParams params = new AdvancedSearchQueryParams();
        params.setStartIndex(10);
        params.setSize(15);
        when(indices.advanced()).thenReturn("advanced-index");
        when(queries.buildAdvancedSearchQuery(params))
                .thenReturn(new AdvancedSearchQueries().buildAdvancedSearchQuery(params));

        AdvancedSearchRequests requests = new AdvancedSearchRequests(restClientService, queries, indices);
        requests.getCompanies(params, "request-id");

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(restClientService).search(requestCaptor.capture());
        SearchRequest request = requestCaptor.getValue();
        assertEquals(List.of("advanced-index"), request.index());
        assertEquals("request-id", request.preference());
        assertEquals(10, request.from());
        assertEquals(15, request.size());
    }
}
