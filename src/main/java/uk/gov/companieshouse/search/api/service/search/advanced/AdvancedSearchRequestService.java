package uk.gov.companieshouse.search.api.service.search.advanced;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.search.api.exception.SearchException;
import uk.gov.companieshouse.search.api.mapper.OpenSearchResponseMapper;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;
import uk.gov.companieshouse.search.api.model.SearchResults;
import uk.gov.companieshouse.search.api.model.TopHit;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.opensearch.AdvancedSearchRequests;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@Service
public class AdvancedSearchRequestService {

    private static final String SEARCH_KIND = "search#advanced-search";

    private final AdvancedSearchRequests advancedSearchRequests;
    private final OpenSearchResponseMapper responseMapper;
    private final ConfiguredIndexNamesProvider indices;

    public AdvancedSearchRequestService(AdvancedSearchRequests advancedSearchRequests,
                                        OpenSearchResponseMapper responseMapper,
                                        ConfiguredIndexNamesProvider indices) {
        this.advancedSearchRequests = advancedSearchRequests;
        this.responseMapper = responseMapper;
        this.indices = indices;
    }

    public SearchResults<Company> getSearchResults(AdvancedSearchQueryParams params, String requestId)
            throws SearchException {
        try {
            SearchResponse<Object> response = advancedSearchRequests.getCompanies(params, requestId);
            List<Hit<Object>> hits = response.hits().hits();
            List<Company> results = new ArrayList<>(hits.size());
            for (Hit<Object> hit : hits) {
                results.add(responseMapper.mapAdvancedSearchResponse(hit));
            }

            TopHit topHit = results.isEmpty() ? new TopHit() : responseMapper.mapAdvancedTopHit(results.getFirst());
            long totalHits = response.hits().total() == null ? 0 : response.hits().total().value();
            return new SearchResults<>(UUID.randomUUID().toString(), topHit, results, SEARCH_KIND, totalHits);
        } catch (IOException e) {
            throw new SearchException("OpenSearch request failed during advanced search for index "
                    + indices.advanced(), e);
        }
    }
}
