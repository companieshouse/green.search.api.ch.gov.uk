package uk.gov.companieshouse.search.api.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
import static org.mockito.Mockito.when;
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

        when(configuredIndexNamesProvider.alphabetical())
                .thenReturn("alpha-index");

        when(environmentReader.getMandatoryInteger("ALPHABETICAL_SEARCH_RESULT_MAX"))
                .thenReturn(20);

        when(environmentReader.getMandatoryInteger("MAX_SIZE_PARAM"))
                .thenReturn(100);

        when(searchIndexService.search("test company", null, null, 10,
                        "request-id"))
                .thenReturn(responseObject);

        when(apiToResponseMapper.map(any()))
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

        when(upsertCompanyService.upsert(any(CompanyProfileApi.class)))
                .thenReturn(responseObject);

        when(apiToResponseMapper.map(any()))
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

        when(apiToResponseMapper.map(any()))
                .thenReturn(ResponseEntity.ok().contentType(APPLICATION_JSON).body(responseObject));

        MvcResult result = mockMvc.perform(put("/alphabetical-search/companies/87654321")
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(company)))
                .andExpect(status().isOk()).andReturn();

        assertTrue(result.getResponse().getContentAsString().contains(ResponseStatus.UPSERT_ERROR.name()));

        verify(upsertCompanyService, Mockito.never()).upsert(any());
    }

    @Test
    void deleteCompanySuccessfully() throws Exception {
        String companyNumber = "12345678";

        ResponseObject<String> responseObject = new ResponseObject<>(ResponseStatus.DOCUMENT_DELETED);

        when(configuredIndexNamesProvider.alphabetical())
                .thenReturn("alpha-index");

        when(alphabeticalSearchDeleteService.deleteCompany(companyNumber))
                .thenReturn(responseObject);

        when(apiToResponseMapper.map(any()))
                .thenReturn(ResponseEntity.ok().contentType(APPLICATION_JSON).body(responseObject));

        mockMvc.perform(delete("/alphabetical-search/companies/{company_number}", companyNumber))
                .andExpect(status().isOk());

        verify(alphabeticalSearchDeleteService, Mockito.times(1)).deleteCompany(companyNumber);
    }

    @Test
    void deleteCompanyNotFoundReturnsDeleteNotFound() throws Exception {
        String companyNumber = "12345678";

        ResponseObject<String> responseObject = new ResponseObject<>(ResponseStatus.DELETE_NOT_FOUND);

        when(configuredIndexNamesProvider.alphabetical())
                .thenReturn("alpha-index");

        when(alphabeticalSearchDeleteService.deleteCompany(companyNumber))
                .thenReturn(responseObject);

        when(apiToResponseMapper.map(any()))
                .thenReturn(ResponseEntity.ok().contentType(APPLICATION_JSON).body(responseObject));

        MvcResult result = mockMvc.perform(delete("/alphabetical-search/companies/{company_number}", companyNumber))
                .andExpect(status().isOk()).andReturn();

        assertTrue(result.getResponse().getContentAsString().contains(ResponseStatus.DELETE_NOT_FOUND.name()));

        verify(alphabeticalSearchDeleteService, Mockito.times(1)).deleteCompany(companyNumber);
    }

    private String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
