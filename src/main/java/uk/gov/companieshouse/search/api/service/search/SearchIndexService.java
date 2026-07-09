package uk.gov.companieshouse.search.api.service.search;

import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;

public interface SearchIndexService {

    /**
     * Searches the OpenSearch database using the specified search parameter.
     *
     * @param searchParam the value used to query the OpenSearch database
     * @return {@link ResponseObject}
     */

    ResponseObject<Company> search(String searchParam, String searchBefore, String searchAfter, Integer size, String requestId);

}
