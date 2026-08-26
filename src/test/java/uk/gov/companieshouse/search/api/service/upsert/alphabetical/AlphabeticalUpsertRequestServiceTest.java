package uk.gov.companieshouse.search.api.service.upsert.alphabetical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.UpdateRequest;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.search.api.model.response.AlphaKeyResponse;
import uk.gov.companieshouse.search.api.opensearch.AlphabeticalSearchUpsertRequest;
import uk.gov.companieshouse.search.api.service.AlphaKeyService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@ExtendWith(MockitoExtension.class)
class AlphabeticalUpsertRequestServiceTest {

    @InjectMocks
    private AlphabeticalUpsertRequestService alphabeticalUpsertRequestService;

    @Mock
    private AlphaKeyService mockAlphaKeyService;

    @Mock
    private AlphabeticalSearchUpsertRequest mockAlphabeticalSearchUpsertRequest;

    @Mock
    private ConfiguredIndexNamesProvider indices;

    private static final String ALPHA_SEARCH = "alpha_search";

    private static final String ID = "ID";
    private static final String COMPANY_TYPE = "company_type";
    private static final String ITEMS = "items";
    private static final String COMPANY_NUMBER = "12345";
    private static final String COMPANY_STATUS = "company_status";
    private static final String CORPORATE_NAME = "corporate_name";
    private static final String RECORD_TYPE = "record_type";
    private static final String RECORD_TYPE_VALUE = "companies";
    private static final String LINKS = "links";
    private static final String SELF = "self";
    private static final String ORDERED_ALPHA_KEY = "ordered_alpha_key";
    private static final String ORDERED_ALPHA_KEY_WITH_ID = "ordered_alpha_key_with_id";

    private static final String ORDERED_ALPHA_KEY_FIELD = "orderedAlphaKey";
    private static final String ORDERED_ALPHA_KEY_WITH_ID_FIELD = "orderedAlphaKey:12345";

    @Test
    @DisplayName("Test create index and update request is successful")
    void testCreateIndexRequestSuccessful() {

        CompanyProfileApi company = createCompany();

        Map<String, Object> requestMap = createRequestMap(company, ORDERED_ALPHA_KEY_FIELD,
                ORDERED_ALPHA_KEY_WITH_ID_FIELD);

        when(mockAlphaKeyService.getAlphaKeyForCorporateName(anyString())).thenReturn(createResponse());
        when(indices.alphabetical()).thenReturn(ALPHA_SEARCH);

        when(mockAlphabeticalSearchUpsertRequest.buildRequest(company, ORDERED_ALPHA_KEY_FIELD,
            ORDERED_ALPHA_KEY_WITH_ID_FIELD))
            .thenReturn(requestMap);

        IndexRequest<Map<String, Object>> indexRequest = alphabeticalUpsertRequestService.createIndexRequest(company);
        UpdateRequest<Object, Map<String, Object>> updateRequest = alphabeticalUpsertRequestService.createUpdateRequest(company, indexRequest);

        assertNotNull(indexRequest);
        assertNotNull(updateRequest);
        assertEquals(ALPHA_SEARCH, indexRequest.index());
        assertEquals(ALPHA_SEARCH, updateRequest.index());
    }

    @Test
    @DisplayName("Test create index request throws exception")
    void testCreateIndexThrowsException() {

        CompanyProfileApi company = createCompany();

        when(mockAlphabeticalSearchUpsertRequest.buildRequest(company, ORDERED_ALPHA_KEY_FIELD,
            ORDERED_ALPHA_KEY_WITH_ID_FIELD)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,
            () -> alphabeticalUpsertRequestService.createIndexRequest(company));
    }

    @Test
    @DisplayName("Test create update request throws exception")
    void testUpdateIndexThrowsException() {

        CompanyProfileApi company = createCompany();

        when(mockAlphabeticalSearchUpsertRequest.buildRequest(company, ORDERED_ALPHA_KEY_FIELD,
            ORDERED_ALPHA_KEY_WITH_ID_FIELD)).thenThrow(RuntimeException.class);
        when(indices.alphabetical()).thenReturn(ALPHA_SEARCH);
        when(mockAlphaKeyService.getAlphaKeyForCorporateName(anyString())).thenReturn(createResponse());

        IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
                .index(ALPHA_SEARCH)
                .document(new HashMap<>()));

        assertThrows(RuntimeException.class,
            () -> alphabeticalUpsertRequestService.createUpdateRequest(company, indexRequest));
    }

    private CompanyProfileApi createCompany() {
        CompanyProfileApi company = new CompanyProfileApi();
        company.setType(COMPANY_TYPE);
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyStatus(COMPANY_STATUS);
        company.setCompanyName(CORPORATE_NAME);

        Map<String, String> links = new HashMap<>();
        links.put("self", "company/00000000");
        company.setLinks(links);

        return company;
    }

    private AlphaKeyResponse createResponse() {
        AlphaKeyResponse alphaKeyResponse = new AlphaKeyResponse();
        alphaKeyResponse.setOrderedAlphaKey(ORDERED_ALPHA_KEY_FIELD);

        return alphaKeyResponse;
    }

    private Map<String, Object> createRequestMap(CompanyProfileApi company, String orderedAlphaKey,
                                                   String orderedAlphaKeyWithID) {
        Map<String, Object> request = new HashMap<>();
        request.put(ID, company.getCompanyNumber());
        request.put(ORDERED_ALPHA_KEY_WITH_ID, orderedAlphaKeyWithID);
        request.put(COMPANY_TYPE, company.getType());

        Map<String, Object> items = new HashMap<>();
        items.put(COMPANY_NUMBER, company.getCompanyNumber());
        items.put(ORDERED_ALPHA_KEY, orderedAlphaKey);
        items.put(COMPANY_STATUS, company.getCompanyStatus());
        items.put(CORPORATE_NAME, company.getCompanyName());
        items.put(RECORD_TYPE, RECORD_TYPE_VALUE);
        request.put(ITEMS, items);

        Map<String, String> links = new HashMap<>();
        links.put(SELF, company.getLinks().get(SELF));
        request.put(LINKS, links);

        return request;
    }
}
