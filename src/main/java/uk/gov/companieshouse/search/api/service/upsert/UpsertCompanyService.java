package uk.gov.companieshouse.search.api.service.upsert;

import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.rest.AlphabeticalSearchRestClientService;
import uk.gov.companieshouse.search.api.service.upsert.alphabetical.AlphabeticalUpsertRequestService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

import java.io.IOException;
import java.util.Map;

import static uk.gov.companieshouse.search.api.logging.LoggingUtils.getLogger;

@Service
public class UpsertCompanyService  {

    private final AlphabeticalSearchRestClientService alphabeticalSearchRestClientService;
    private final AlphabeticalUpsertRequestService alphabeticalUpsertRequestService;

    private final ConfiguredIndexNamesProvider indices;

    public UpsertCompanyService(
            AlphabeticalSearchRestClientService alphabeticalSearchRestClientService,
            AlphabeticalUpsertRequestService alphabeticalUpsertRequestService,
            ConfiguredIndexNamesProvider indices) {
        this.alphabeticalSearchRestClientService = alphabeticalSearchRestClientService;
        this.alphabeticalUpsertRequestService = alphabeticalUpsertRequestService;
        this.indices = indices;
    }

    /**
     * Upserts a new document to the alphabetical search index.
     * If a document does not exist it is added.
     * If the document does exist it is updated.
     *
     * @param company - Company sent over in REST call to be added/updated
     * @return {@link ResponseObject}
     */
    public ResponseObject<String> upsert(CompanyProfileApi company) {
        Map<String, Object> logMap = new DataMap.Builder()
                .companyName(company.getCompanyName())
                .companyNumber(company.getCompanyNumber())
                .indexName(indices.alphabetical())
                .build().getLogMap();
        getLogger().info("OpenSearch Upserting company underway", logMap);

        UpdateRequest<Object, Map<String, Object>> updateRequest;

        IndexRequest<Map<String, Object>> indexRequest = alphabeticalUpsertRequestService.createIndexRequest(company);
        updateRequest = alphabeticalUpsertRequestService.createUpdateRequest(company, indexRequest);

        try {
            alphabeticalSearchRestClientService.upsert(updateRequest);
        } catch (IOException e) {
            getLogger().error("IOException when upserting company on OpenSearch", logMap);
            return new ResponseObject<>(ResponseStatus.UPDATE_REQUEST_ERROR);
        }

        getLogger().info("OpenSearch Upsert successful for ", logMap);
        return new ResponseObject<>(ResponseStatus.DOCUMENT_UPSERTED);
    }

}
