package uk.gov.companieshouse.search.api.service.search.advanced;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.search.api.exception.SearchException;
import uk.gov.companieshouse.search.api.logging.LoggingUtils;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;
import uk.gov.companieshouse.search.api.model.SearchResults;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;

@Service
public class AdvancedSearchIndexService {

    private final AdvancedSearchRequestService requestService;

    public AdvancedSearchIndexService(AdvancedSearchRequestService requestService) {
        this.requestService = requestService;
    }

    public ResponseObject<Company> searchAdvanced(AdvancedSearchQueryParams params, String requestId) {
        SearchResults<Company> results;
        try {
            results = requestService.getSearchResults(params, requestId);
        } catch (SearchException e) {
            LoggingUtils.getLogger().error("An error occurred while advanced searching for a company", e);
            return new ResponseObject<>(ResponseStatus.SEARCH_ERROR, null);
        }
        if (!results.items().isEmpty()) {
            return new ResponseObject<>(ResponseStatus.SEARCH_FOUND, results);
        }
        return new ResponseObject<>(ResponseStatus.SEARCH_NOT_FOUND, null);
    }
}
