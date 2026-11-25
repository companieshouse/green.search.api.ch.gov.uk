package uk.gov.companieshouse.search.api.service.rest;

import java.io.IOException;
import java.util.Map;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.core.UpdateResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AdvancedSearchRestClientService implements RestClientService {

    private final OpenSearchClient advancedSearchClient;

    public AdvancedSearchRestClientService(
            @Qualifier("advancedSearchRestClient") OpenSearchClient advancedSearchClient) {
        this.advancedSearchClient = advancedSearchClient;
    }

    @Override
    public SearchResponse<Object> search(SearchRequest searchRequest) throws IOException {
        return advancedSearchClient.search(searchRequest, Object.class);
    }

    @Override
    public UpdateResponse<Object> upsert(UpdateRequest<Object, Map<String, Object>> updateRequest)
            throws IOException {
        return advancedSearchClient.update(updateRequest, Object.class);
    }

    public DeleteResponse delete(DeleteRequest deleteRequest) throws IOException {
        return advancedSearchClient.delete(deleteRequest);
    }
}
