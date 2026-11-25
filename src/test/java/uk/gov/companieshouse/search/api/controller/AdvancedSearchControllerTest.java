package uk.gov.companieshouse.search.api.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.search.api.mapper.AdvancedQueryParamMapper;
import uk.gov.companieshouse.search.api.mapper.ApiToResponseMapper;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;
import uk.gov.companieshouse.search.api.model.SearchResults;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.delete.advanced.AdvancedSearchDeleteService;
import uk.gov.companieshouse.search.api.service.search.advanced.AdvancedSearchIndexService;
import uk.gov.companieshouse.search.api.service.upsert.UpsertCompanyService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@WebMvcTest(AdvancedSearchController.class)
class AdvancedSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdvancedQueryParamMapper queryParamMapper;

    @MockitoBean
    private AdvancedSearchIndexService searchIndexService;

    @MockitoBean
    private ApiToResponseMapper apiToResponseMapper;

    @MockitoBean
    private UpsertCompanyService upsertCompanyService;

    @MockitoBean
    private ConfiguredIndexNamesProvider indices;

    @MockitoBean
    private AdvancedSearchDeleteService deleteService;

    @Test
    void searchesAdvancedIndexWithMappedParameters() throws Exception {
        AdvancedSearchQueryParams params = new AdvancedSearchQueryParams();
        params.setSize(20);
        ResponseObject<Company> searchResponse = new ResponseObject<>(
                ResponseStatus.SEARCH_FOUND,
                new SearchResults<>("", null, List.of(), "search#advanced-search", 0L));
        when(indices.advanced()).thenReturn("advanced-index");
        when(queryParamMapper.mapAdvancedQueryParameters(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(params);
        when(searchIndexService.searchAdvanced(params, "request-id")).thenReturn(searchResponse);
        when(apiToResponseMapper.map(any())).thenReturn(ResponseEntity.ok("mapped"));

        mockMvc.perform(get("/advanced-search/companies")
                        .param("company_name_includes", "Example Ltd")
                        .param("company_status", "active")
                        .header("X-Request-ID", "request-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("mapped"));

        verify(searchIndexService).searchAdvanced(params, "request-id");
    }

    @Test
    void exportsSuccessfulAdvancedSearchAsCsv() throws Exception {
        AdvancedSearchQueryParams params = new AdvancedSearchQueryParams();
        params.setSize(20);
        Company company = Company.builder()
                .companyName("Example, Limited")
                .companyNumber("12345678")
                .companyStatus("active")
                .build();
        ResponseObject<Company> searchResponse = new ResponseObject<>(
                ResponseStatus.SEARCH_FOUND,
                new SearchResults<>("", null, List.of(company), "search#advanced-search", 1L));
        when(indices.advanced()).thenReturn("advanced-index");
        when(queryParamMapper.mapAdvancedQueryParameters(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(params);
        when(searchIndexService.searchAdvanced(params, "request-id")).thenReturn(searchResponse);
        when(apiToResponseMapper.map(any())).thenReturn(ResponseEntity.ok("mapped"));

        mockMvc.perform(get("/advanced-search/csv")
                        .param("company_name_includes", "Example")
                        .header("X-Request-ID", "request-id"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"Example, Limited\",12345678")));
    }

    @Test
    void mapsInvalidDateToDateFormatError() throws Exception {
        when(indices.advanced()).thenReturn("advanced-index");
        when(queryParamMapper.mapAdvancedQueryParameters(
                any(), any(), any(), eq("bad-date"), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new uk.gov.companieshouse.search.api.exception.DateFormatException("bad date"));
        when(apiToResponseMapper.map(any())).thenAnswer(invocation -> {
            ResponseObject<?> response = invocation.getArgument(0);
            assertTrue(response.getStatus() == ResponseStatus.DATE_FORMAT_ERROR);
            return ResponseEntity.badRequest().build();
        });

        mockMvc.perform(get("/advanced-search/companies")
                        .param("incorporated_from", "bad-date")
                        .header("X-Request-ID", "request-id"))
                .andExpect(status().isBadRequest());
    }
}
