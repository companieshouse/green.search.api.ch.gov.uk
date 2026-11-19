package uk.gov.companieshouse.search.api.service.upsert.advanced;

import java.util.Map;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.search.api.opensearch.AdvancedSearchUpsertRequest;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@Service
public class AdvancedUpsertRequestService {

    private final AdvancedSearchUpsertRequest advancedSearchUpsertRequest;
    private final ConfiguredIndexNamesProvider indices;

    public AdvancedUpsertRequestService(AdvancedSearchUpsertRequest advancedSearchUpsertRequest,
                                        ConfiguredIndexNamesProvider indices) {
        this.advancedSearchUpsertRequest = advancedSearchUpsertRequest;
        this.indices = indices;
    }

    public UpdateRequest<Object, Map<String, Object>> createUpdateRequest(
            CompanyProfileApi company, String orderedAlphaKey, String sameAsKey) {
        Map<String, Object> document =
                advancedSearchUpsertRequest.buildRequest(company, orderedAlphaKey, sameAsKey);
        return UpdateRequest.of(u -> u.index(indices.advanced())
                .id(company.getCompanyNumber())
                .doc(document)
                .docAsUpsert(true));
    }
}
