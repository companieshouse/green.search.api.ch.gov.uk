package uk.gov.companieshouse.search.api.service.delete.advanced;

import java.io.IOException;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.search.api.logging.LoggingUtils;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.rest.AdvancedSearchRestClientService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@Service
public class AdvancedSearchDeleteService {

    private final ConfiguredIndexNamesProvider indices;
    private final AdvancedSearchRestClientService restClientService;

    public AdvancedSearchDeleteService(ConfiguredIndexNamesProvider indices,
                                       AdvancedSearchRestClientService restClientService) {
        this.indices = indices;
        this.restClientService = restClientService;
    }

    public ResponseObject<String> deleteCompanyByNumber(String companyNumber) {
        DeleteRequest request = new DeleteRequest.Builder()
                .index(indices.advanced())
                .id(companyNumber)
                .build();
        try {
            if (restClientService.delete(request).result() == Result.NotFound) {
                return new ResponseObject<>(ResponseStatus.DELETE_NOT_FOUND);
            }
            return new ResponseObject<>(ResponseStatus.DOCUMENT_DELETED);
        } catch (IOException e) {
            LoggingUtils.getLogger().error("OpenSearch transport failure deleting advanced-search company", e);
            return new ResponseObject<>(ResponseStatus.SERVICE_UNAVAILABLE);
        } catch (OpenSearchException e) {
            LoggingUtils.getLogger().error("OpenSearch rejected advanced-search company deletion", e);
            return new ResponseObject<>(ResponseStatus.DELETE_REQUEST_ERROR);
        }
    }
}
