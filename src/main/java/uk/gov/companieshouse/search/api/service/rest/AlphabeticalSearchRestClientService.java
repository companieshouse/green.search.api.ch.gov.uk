package uk.gov.companieshouse.search.api.service.rest;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.core.UpdateResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class AlphabeticalSearchRestClientService implements RestClientService {

    private final OpenSearchClient alphabeticalSearchClient;

    public AlphabeticalSearchRestClientService(OpenSearchClient alphabeticalSearchClient) {
        this.alphabeticalSearchClient = alphabeticalSearchClient;
    }

    @Override
    public SearchResponse<Object> search(SearchRequest searchRequest) throws IOException {
        return alphabeticalSearchClient.search(searchRequest, Object.class);
    }

    @Override
    public UpdateResponse<Object> upsert(UpdateRequest<Object, Map<String, Object>> updateRequest) throws IOException {
        return alphabeticalSearchClient.update(updateRequest, Object.class);
    }

    public DeleteResponse delete(DeleteRequest deleteRequest) throws IOException {
        return alphabeticalSearchClient.delete(deleteRequest);
    }
}
