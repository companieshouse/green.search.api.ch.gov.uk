package uk.gov.companieshouse.search.api.service.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.search.api.model.response.ResponseStatus.DOCUMENT_UPSERTED;
import static uk.gov.companieshouse.search.api.model.response.ResponseStatus.UPDATE_REQUEST_ERROR;
import static uk.gov.companieshouse.search.api.model.response.ResponseStatus.UPSERT_ERROR;

import java.io.IOException;
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
import uk.gov.companieshouse.search.api.exception.UpsertException;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.service.rest.AlphabeticalSearchRestClientService;
import uk.gov.companieshouse.search.api.service.upsert.alphabetical.AlphabeticalUpsertRequestService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@ExtendWith(MockitoExtension.class)
class UpsertCompanyServiceTest {
    @Mock
    private AlphabeticalSearchRestClientService mockAlphabeticalRestClientService;

    @Mock
    private AlphabeticalUpsertRequestService mockAlphabeticalUpsertRequestService;

    @Mock
    private UpdateRequest<Object, Map<String, Object>> updateRequest;

    @Mock
    private ConfiguredIndexNamesProvider indices;

    @InjectMocks
    private UpsertCompanyService upsertCompanyService;


    @Test
    @DisplayName("Test upsert is successful")
    void testUpsertIsSuccessful() throws Exception {

        CompanyProfileApi company = createCompany();
        IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
                .index("alpha_search")
                .document(new HashMap<>()));

        when(mockAlphabeticalUpsertRequestService.createIndexRequest(company)).thenReturn(indexRequest);
        when(mockAlphabeticalUpsertRequestService.createUpdateRequest(
            company, indexRequest)).thenReturn(updateRequest);

        ResponseObject<?> responseObject = upsertCompanyService.upsert(company);

        assertNotNull(responseObject);
        assertEquals(DOCUMENT_UPSERTED, responseObject.getStatus());
    }


    @Test
    @DisplayName("Test exception thrown during index request")
    void testExceptionThrownDuringIndexRequest() throws Exception {

        CompanyProfileApi company = createCompany();

        when(mockAlphabeticalUpsertRequestService.createIndexRequest(company)).thenThrow(UpsertException.class);

        ResponseObject<?> responseObject = upsertCompanyService.upsert(company);

        assertNotNull(responseObject);
        assertEquals(UPSERT_ERROR, responseObject.getStatus());
    }

    @Test
    @DisplayName("Test exception thrown during update request")
    void testExceptionThrownDuringUpdateRequest() throws Exception {

        CompanyProfileApi company = createCompany();
        IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
                .index("alpha_search")
                .document(new HashMap<>()));

        when(mockAlphabeticalUpsertRequestService.createIndexRequest(company)).thenReturn(indexRequest);
        when(mockAlphabeticalUpsertRequestService.createUpdateRequest(
            company, indexRequest)).thenThrow(UpsertException.class);

        ResponseObject<?> responseObject = upsertCompanyService.upsert(company);

        assertNotNull(responseObject);
        assertEquals(UPSERT_ERROR, responseObject.getStatus());
    }

    @Test
    @DisplayName("Test exception thrown during upsert")
    void testExceptionThrownDuringUpsert() throws Exception {

        CompanyProfileApi company = createCompany();
        IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
                .index("alpha_search")
                .document(new HashMap<>()));

        when(mockAlphabeticalUpsertRequestService.createIndexRequest(company)).thenReturn(indexRequest);
        when(mockAlphabeticalUpsertRequestService.createUpdateRequest(
            company, indexRequest)).thenReturn(updateRequest);

        when(mockAlphabeticalRestClientService.upsert(updateRequest)).thenThrow(IOException.class);

        ResponseObject<?> responseObject = upsertCompanyService.upsert(company);

        assertNotNull(responseObject);
        assertEquals(UPDATE_REQUEST_ERROR, responseObject.getStatus());
    }


    private CompanyProfileApi createCompany() {
        CompanyProfileApi company = new CompanyProfileApi();
        company.setType("company type");
        company.setCompanyNumber("company number");
        company.setCompanyStatus("company status");
        company.setCompanyName("company name");

        Map<String, String> links = new HashMap<>();
        links.put("self", "company/00000000");
        company.setLinks(links);

        return company;
    }
}
