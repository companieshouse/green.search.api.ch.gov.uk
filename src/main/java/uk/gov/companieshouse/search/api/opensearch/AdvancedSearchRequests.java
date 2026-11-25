package uk.gov.companieshouse.search.api.opensearch;

import java.io.IOException;
import java.util.Map;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.search.api.logging.LoggingUtils;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;
import uk.gov.companieshouse.search.api.service.rest.AdvancedSearchRestClientService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@Component
public class AdvancedSearchRequests {

    private final AdvancedSearchRestClientService restClientService;
    private final AdvancedSearchQueries advancedSearchQueries;
    private final ConfiguredIndexNamesProvider indices;

    public AdvancedSearchRequests(AdvancedSearchRestClientService restClientService,
                                  AdvancedSearchQueries advancedSearchQueries,
                                  ConfiguredIndexNamesProvider indices) {
        this.restClientService = restClientService;
        this.advancedSearchQueries = advancedSearchQueries;
        this.indices = indices;
    }

    public SearchResponse<Object> getCompanies(AdvancedSearchQueryParams params, String requestId)
            throws IOException {
        Map<String, Object> logMap = new DataMap.Builder().requestId(requestId).build().getLogMap();
        LoggingUtils.getLogger().info("Building advanced search request", logMap);

        SearchRequest request = SearchRequest.of(s -> s
                .index(indices.advanced())
                .preference(requestId)
                .from(params.getStartIndex())
                .size(params.getSize())
                .trackTotalHits(t -> t.enabled(true))
                .query(advancedSearchQueries.buildAdvancedSearchQuery(params)));
        return restClientService.search(request);
    }
}
