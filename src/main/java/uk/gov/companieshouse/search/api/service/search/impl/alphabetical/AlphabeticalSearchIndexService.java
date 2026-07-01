package uk.gov.companieshouse.search.api.service.search.impl.alphabetical;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.search.api.model.esdatamodel.Company;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.search.SearchIndexService;

@Service
public class AlphabeticalSearchIndexService implements SearchIndexService {


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseObject<Company> search(String corporateName, String searchBefore, String searchAfter, Integer size,
                                          String requestId) {
        return new ResponseObject<>(ResponseStatus.SEARCH_NOT_FOUND, null);
    }
}
