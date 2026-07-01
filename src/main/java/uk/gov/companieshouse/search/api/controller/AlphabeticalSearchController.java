package uk.gov.companieshouse.search.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.search.api.exception.SizeException;
import uk.gov.companieshouse.search.api.mapper.ApiToResponseMapper;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.search.SearchIndexService;
import uk.gov.companieshouse.search.api.service.search.SearchRequestUtils;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

import java.util.Map;

import static uk.gov.companieshouse.search.api.logging.LoggingUtils.getLogger;


@RestController
@RequestMapping(value = "/alphabetical-search", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlphabeticalSearchController {

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final String COMPANY_NAME_QUERY_PARAM = "q";
    private static final String SEARCH_BEFORE_PARAM = "search_before";
    private static final String SEARCH_AFTER_PARAM = "search_after";
    private static final String SIZE_PARAM = "size";
    private static final String MAX_SIZE_PARAM = "MAX_SIZE_PARAM";
    private static final String ALPHABETICAL_SEARCH_RESULT_MAX = "ALPHABETICAL_SEARCH_RESULT_MAX";

    private final SearchIndexService searchIndexService;

    private final ApiToResponseMapper apiToResponseMapper;
    private final EnvironmentReader environmentReader;
    private final ConfiguredIndexNamesProvider indices;

    public AlphabeticalSearchController(SearchIndexService searchIndexService,
                                        ApiToResponseMapper apiToResponseMapper,
                                        EnvironmentReader environmentReader, ConfiguredIndexNamesProvider indices) {
        this.searchIndexService = searchIndexService;
        this.apiToResponseMapper = apiToResponseMapper;
        this.environmentReader = environmentReader;
        this.indices = indices;
    }

    @GetMapping("/companies")
    public ResponseEntity<Object> searchByCorporateName(@RequestParam(name = COMPANY_NAME_QUERY_PARAM) String companyName,
                                                   @RequestParam(name = SEARCH_BEFORE_PARAM, required = false) String searchBefore,
                                                   @RequestParam(name = SEARCH_AFTER_PARAM, required = false) String searchAfter,
                                                   @RequestParam(name = SIZE_PARAM, required = false) Integer size,
                                                   @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {

        Map<String, Object> logMap = new DataMap.Builder()
                .requestId(requestId)
                .companyName(companyName)
                .indexName(indices.alphabetical())
                .searchBefore(searchBefore)
                .searchAfter(searchAfter)
                .size(String.valueOf(size))
                .build().getLogMap();
        getLogger().info("Search request received", logMap);

        try {
            size = SearchRequestUtils.checkResultsSize
                (size, environmentReader.getMandatoryInteger(ALPHABETICAL_SEARCH_RESULT_MAX),
                    environmentReader.getMandatoryInteger(MAX_SIZE_PARAM));
        } catch (SizeException e) {
            getLogger().info(e.getMessage(), logMap);
            return apiToResponseMapper
                .map(new ResponseObject<String>(ResponseStatus.SIZE_PARAMETER_ERROR, null));
        }

        ResponseObject<?> responseObject = searchIndexService
            .search(companyName, searchBefore, searchAfter, size, requestId);

        return apiToResponseMapper.map(responseObject);
    }

}
