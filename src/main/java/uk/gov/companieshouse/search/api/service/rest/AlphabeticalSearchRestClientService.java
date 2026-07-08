package uk.gov.companieshouse.search.api.service.rest;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.*;
import org.springframework.stereotype.Service;

import java.io.IOException;


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

}
