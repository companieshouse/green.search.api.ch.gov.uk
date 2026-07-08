package uk.gov.companieshouse.search.api.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.search.api.mapper.ApiToResponseMapper;
import uk.gov.companieshouse.search.api.model.SearchResults;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.search.SearchIndexService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlphabeticalSearchController.class)
class AlphabeticalSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchIndexService searchIndexService;

    @MockitoBean
    private ApiToResponseMapper apiToResponseMapper;

    @MockitoBean
    private EnvironmentReader environmentReader;

    @MockitoBean
    private ConfiguredIndexNamesProvider configuredIndexNamesProvider;

    @Test
    void searchByCorporateNameReturnsOk() throws Exception {

        ResponseObject<Company> responseObject =
                new ResponseObject<>(ResponseStatus.SEARCH_FOUND, new SearchResults<>());

        Mockito.when(configuredIndexNamesProvider.alphabetical())
                .thenReturn("alpha-index");

        Mockito.when(environmentReader.getMandatoryInteger("ALPHABETICAL_SEARCH_RESULT_MAX"))
                .thenReturn(20);

        Mockito.when(environmentReader.getMandatoryInteger("MAX_SIZE_PARAM"))
                .thenReturn(100);

        Mockito.when(searchIndexService.search(eq("test company"), eq(null), eq(null), eq(10),
                        eq("request-id")))
                .thenReturn(responseObject);

        Mockito.when(apiToResponseMapper.map(any()))
                .thenReturn(ResponseEntity.ok().contentType(APPLICATION_JSON).body(responseObject));

        mockMvc.perform(get("/alphabetical-search/companies")
                        .param("q", "test company")
                        .param("size", "10")
                        .header("X-Request-ID", "request-id"))
                .andExpect(status().isOk());
    }

    @Test
    void searchByCorporateNameMissingRequestIdReturnsBadRequest() throws Exception {

        mockMvc.perform(get("/alphabetical-search/companies")
                        .param("q", "test company"))
                .andExpect(status().isBadRequest());
    }
}
