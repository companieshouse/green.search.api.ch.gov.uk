package uk.gov.companieshouse.search.api.service.delete.alphabetical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.rest.AlphabeticalSearchRestClientService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class AlphabeticalSearchDeleteServiceTest {

    private static final String INDEX = "alphabetical_search";

    private static final String TEST_COMPANY_NUMBER = "00002500";

    @Mock
    AlphabeticalSearchRestClientService alphabeticalSearchRestClientService;

    @Mock
    private ConfiguredIndexNamesProvider indices;

    @InjectMocks
    AlphabeticalSearchDeleteService service;

    @BeforeEach
    void setUp() {
        when(indices.alphabetical()).thenReturn(INDEX);
    }

    @Test
    void deletesCompany() throws IOException {

        DeleteResponse deleteResponse = mock(DeleteResponse.class);
        when(deleteResponse.result()).thenReturn(Result.Deleted);

        when(alphabeticalSearchRestClientService.delete(any(DeleteRequest.class))).thenReturn(deleteResponse);

        ResponseObject<?> response = service.deleteCompany(TEST_COMPANY_NUMBER);

        assertEquals(ResponseStatus.DOCUMENT_DELETED, response.getStatus());

    }

    @Test
    void returnsDeleteNotFoundWhenCompanyDoesNotExist() throws IOException {
        DeleteResponse deleteResponse = mock(DeleteResponse.class);
        when(deleteResponse.result()).thenReturn(Result.NotFound);

        when(alphabeticalSearchRestClientService.delete(any(DeleteRequest.class))).thenReturn(deleteResponse);

        ResponseObject<?> response = service.deleteCompany(TEST_COMPANY_NUMBER);

        assertEquals(ResponseStatus.DELETE_NOT_FOUND, response.getStatus());
    }

    @Test
    void returnsServiceUnavailableOnIOException() throws IOException {

        when(alphabeticalSearchRestClientService.delete(any(DeleteRequest.class))).thenThrow(IOException.class);

        ResponseObject<?> response = service.deleteCompany(TEST_COMPANY_NUMBER);

        assertEquals(ResponseStatus.SERVICE_UNAVAILABLE, response.getStatus());
    }

    @Test
    void returnsUpdateErrorOnOpenSearchException() throws Exception {

        when(alphabeticalSearchRestClientService.delete(any(DeleteRequest.class)))
                .thenThrow(mock(OpenSearchException.class));

        ResponseObject<?> response = service.deleteCompany(TEST_COMPANY_NUMBER);

        assertEquals(ResponseStatus.DELETE_REQUEST_ERROR, response.getStatus());
    }

}
