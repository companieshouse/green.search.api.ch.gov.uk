package uk.gov.companieshouse.search.api.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.search.api.mapper.ApiToResponseMapper;
import uk.gov.companieshouse.search.api.model.SearchResults;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.delete.alphabetical.AlphabeticalSearchDeleteService;
import uk.gov.companieshouse.search.api.service.search.SearchIndexService;
import uk.gov.companieshouse.search.api.service.upsert.UpsertCompanyService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlphabeticalSearchController.class)
class AlphabeticalSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchIndexService searchIndexService;

    @MockitoBean
    private UpsertCompanyService upsertCompanyService;

    @MockitoBean
    private AlphabeticalSearchDeleteService alphabeticalSearchDeleteService;

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

        Mockito.when(searchIndexService.search("test company", null, null, 10,
                        "request-id"))
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

    @Test
    void upsertCompanySuccessfully() throws Exception {
        CompanyProfileApi company = new CompanyProfileApi();
        company.setCompanyNumber("12345678");
        company.setCompanyName("Test Company Ltd");

        ResponseObject<String> responseObject = new ResponseObject<>(ResponseStatus.DOCUMENT_UPSERTED);

        Mockito.when(upsertCompanyService.upsert(any(CompanyProfileApi.class)))
                .thenReturn(responseObject);

        Mockito.when(apiToResponseMapper.map(any()))
                .thenReturn(ResponseEntity.ok().contentType(APPLICATION_JSON).body(responseObject));

        mockMvc.perform(put("/alphabetical-search/companies/12345678")
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(company)))
                .andExpect(status().isOk());

        verify(upsertCompanyService, Mockito.times(1)).upsert(any(CompanyProfileApi.class));
    }

    @Test
    void upsertCompanyWithMismatchedNumberReturnsBadRequest() throws Exception {
        CompanyProfileApi company = new CompanyProfileApi();
        company.setCompanyNumber("12345678");
        company.setCompanyName("Test Company Ltd");

        ResponseObject<String> responseObject = new ResponseObject<>(ResponseStatus.UPSERT_ERROR);

        Mockito.when(apiToResponseMapper.map(any()))
                .thenReturn(ResponseEntity.ok().contentType(APPLICATION_JSON).body(responseObject));

        ResultActions resultActions = mockMvc.perform(put("/alphabetical-search/companies/87654321")
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(company)))
                .andExpect(status().isOk());

        assertTrue(resultActions.andReturn().getResponse().getContentAsString().contains(ResponseStatus.UPSERT_ERROR.name()));

        verify(upsertCompanyService, Mockito.never()).upsert(any());
    }

    @Test
    void upsertCompanyWithEmptyNumberReturnsBadRequest() throws Exception {
        CompanyProfileApi company = new CompanyProfileApi();
        company.setCompanyNumber("12345678");
        company.setCompanyName("Test Company Ltd");

        ResponseObject<String> responseObject = new ResponseObject<>(ResponseStatus.UPSERT_ERROR);

        Mockito.when(apiToResponseMapper.map(any()))
                .thenReturn(ResponseEntity.ok().contentType(APPLICATION_JSON).body(responseObject));

        mockMvc.perform(put("/alphabetical-search/companies/")
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(company)))
                .andExpect(status().isNotFound());
    }

    private String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
