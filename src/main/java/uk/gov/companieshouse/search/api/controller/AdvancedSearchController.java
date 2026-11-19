package uk.gov.companieshouse.search.api.controller;

import static uk.gov.companieshouse.search.api.logging.LoggingUtils.getLogger;
import static uk.gov.companieshouse.search.api.util.CsvUtils.escapeReservedCsvCharacters;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.search.api.exception.DateFormatException;
import uk.gov.companieshouse.search.api.exception.MappingException;
import uk.gov.companieshouse.search.api.exception.SizeException;
import uk.gov.companieshouse.search.api.mapper.AdvancedQueryParamMapper;
import uk.gov.companieshouse.search.api.mapper.ApiToResponseMapper;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;
import uk.gov.companieshouse.search.api.model.data.Address;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.response.ResponseObject;
import uk.gov.companieshouse.search.api.model.response.ResponseStatus;
import uk.gov.companieshouse.search.api.service.delete.advanced.AdvancedSearchDeleteService;
import uk.gov.companieshouse.search.api.service.search.advanced.AdvancedSearchIndexService;
import uk.gov.companieshouse.search.api.service.upsert.UpsertCompanyService;
import uk.gov.companieshouse.search.api.util.ConfiguredIndexNamesProvider;

@RestController
@RequestMapping(value = "/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdvancedSearchController {

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private final AdvancedQueryParamMapper queryParamMapper;
    private final AdvancedSearchIndexService searchIndexService;
    private final ApiToResponseMapper apiToResponseMapper;
    private final UpsertCompanyService upsertCompanyService;
    private final ConfiguredIndexNamesProvider indices;
    private final AdvancedSearchDeleteService deleteService;

    public AdvancedSearchController(AdvancedQueryParamMapper queryParamMapper,
                                    AdvancedSearchIndexService searchIndexService,
                                    ApiToResponseMapper apiToResponseMapper,
                                    UpsertCompanyService upsertCompanyService,
                                    ConfiguredIndexNamesProvider indices,
                                    AdvancedSearchDeleteService deleteService) {
        this.queryParamMapper = queryParamMapper;
        this.searchIndexService = searchIndexService;
        this.apiToResponseMapper = apiToResponseMapper;
        this.upsertCompanyService = upsertCompanyService;
        this.indices = indices;
        this.deleteService = deleteService;
    }

    @GetMapping("/companies")
    public ResponseEntity<Object> search(
            @RequestParam(name = "start_index", required = false) Integer startIndex,
            @RequestParam(name = "company_name_includes", required = false) String companyName,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "incorporated_from", required = false) String incorporatedFrom,
            @RequestParam(name = "incorporated_to", required = false) String incorporatedTo,
            @RequestParam(name = "company_status", required = false) List<String> companyStatusList,
            @RequestParam(name = "sic_codes", required = false) List<String> sicCodes,
            @RequestParam(name = "company_type", required = false) List<String> companyTypeList,
            @RequestParam(name = "company_subtype", required = false) List<String> companySubtypeList,
            @RequestParam(name = "dissolved_from", required = false) String dissolvedFrom,
            @RequestParam(name = "dissolved_to", required = false) String dissolvedTo,
            @RequestParam(name = "company_name_excludes", required = false) String companyNameExcludes,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        return apiToResponseMapper.map(searchCompanies(startIndex, companyName, location, incorporatedFrom,
                incorporatedTo, companyStatusList, sicCodes, companyTypeList, companySubtypeList,
                dissolvedFrom, dissolvedTo, companyNameExcludes, size, requestId));
    }

    @GetMapping("/csv")
    public ResponseEntity<Object> searchToCsv(
            @RequestParam(name = "start_index", required = false) Integer startIndex,
            @RequestParam(name = "company_name_includes", required = false) String companyName,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "incorporated_from", required = false) String incorporatedFrom,
            @RequestParam(name = "incorporated_to", required = false) String incorporatedTo,
            @RequestParam(name = "company_status", required = false) List<String> companyStatusList,
            @RequestParam(name = "sic_codes", required = false) List<String> sicCodes,
            @RequestParam(name = "company_type", required = false) List<String> companyTypeList,
            @RequestParam(name = "company_subtype", required = false) List<String> companySubtypeList,
            @RequestParam(name = "dissolved_from", required = false) String dissolvedFrom,
            @RequestParam(name = "dissolved_to", required = false) String dissolvedTo,
            @RequestParam(name = "company_name_excludes", required = false) String companyNameExcludes,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        ResponseObject<Company> response = searchCompanies(startIndex, companyName, location,
                incorporatedFrom, incorporatedTo, companyStatusList, sicCodes, companyTypeList,
                companySubtypeList, dissolvedFrom, dissolvedTo, companyNameExcludes, size, requestId);
        ResponseEntity<Object> mapped = apiToResponseMapper.map(response);
        if (response.getStatus() != ResponseStatus.SEARCH_FOUND || mapped.getStatusCode() != HttpStatus.OK) {
            return mapped;
        }
        return ResponseEntity.ok().body(getCsvFromResponse(response));
    }

    @PutMapping("/companies/{company_number}")
    public ResponseEntity<Object> upsert(@PathVariable("company_number") String companyNumber,
                                         @Valid @RequestBody CompanyProfileApi company) {
        Map<String, Object> logMap = new DataMap.Builder()
                .companyName(company.getCompanyName())
                .companyNumber(company.getCompanyNumber())
                .upsertCompanyNumber(companyNumber)
                .indexName(indices.advanced())
                .build().getLogMap();
        getLogger().info("Attempting to upsert a company to advanced search index", logMap);

        if (companyNumber == null || companyNumber.isEmpty()
                || !companyNumber.equalsIgnoreCase(company.getCompanyNumber())) {
            return apiToResponseMapper.map(new ResponseObject<>(ResponseStatus.UPSERT_ERROR));
        }
        return apiToResponseMapper.map(upsertCompanyService.upsertAdvanced(company));
    }

    @DeleteMapping("/companies/{company_number}")
    public ResponseEntity<Object> deleteCompany(@PathVariable("company_number") String companyNumber) {
        if (companyNumber == null || companyNumber.isEmpty()) {
            return apiToResponseMapper.map(new ResponseObject<>(ResponseStatus.DELETE_NOT_FOUND));
        }
        return apiToResponseMapper.map(deleteService.deleteCompanyByNumber(companyNumber));
    }

    private ResponseObject<Company> searchCompanies(
            Integer startIndex, String companyName, String location, String incorporatedFrom,
            String incorporatedTo, List<String> companyStatusList, List<String> sicCodes,
            List<String> companyTypeList, List<String> companySubtypeList, String dissolvedFrom,
            String dissolvedTo, String companyNameExcludes, Integer size, String requestId) {
        Map<String, Object> logMap = new DataMap.Builder()
                .requestId(requestId)
                .companyName(companyName)
                .startIndex(String.valueOf(startIndex))
                .location(location)
                .companyStatus(companyStatusList)
                .sicCodes(sicCodes)
                .companyType(companyTypeList)
                .companySubtype(companySubtypeList)
                .companyNameExcludes(companyNameExcludes)
                .size(String.valueOf(size))
                .indexName(indices.advanced())
                .build().getLogMap();
        logMap.put("incorporated_from", incorporatedFrom);
        logMap.put("incorporated_to", incorporatedTo);
        logMap.put("dissolved_from", dissolvedFrom);
        logMap.put("dissolved_to", dissolvedTo);
        getLogger().info("Advanced search request received", logMap);

        try {
            AdvancedSearchQueryParams params = queryParamMapper.mapAdvancedQueryParameters(
                    startIndex, companyName, location, incorporatedFrom, incorporatedTo,
                    companyStatusList, sicCodes, companyTypeList, companySubtypeList,
                    dissolvedFrom, dissolvedTo, companyNameExcludes, size);
            return searchIndexService.searchAdvanced(params, requestId);
        } catch (DateFormatException e) {
            return new ResponseObject<>(ResponseStatus.DATE_FORMAT_ERROR, null);
        } catch (MappingException e) {
            return new ResponseObject<>(ResponseStatus.MAPPING_ERROR, null);
        } catch (SizeException e) {
            return new ResponseObject<>(ResponseStatus.ADVANCED_SIZE_PARAMETER_ERROR, null);
        }
    }

    private String getCsvFromResponse(ResponseObject<Company> response) {
        StringBuilder csv = new StringBuilder("company_name,company_number,company_status,company_type,"
                + "company_subtype,dissolution_date,incorporation_date,removed_date,registered_date,"
                + "nature_of_business,registered_office_address\n");
        for (Company company : response.getData().items()) {
            csv.append(escapeReservedCsvCharacters(company.companyName())).append(',')
                    .append(checkNull(company.companyNumber())).append(',')
                    .append(checkNull(company.companyStatus())).append(',')
                    .append(checkNull(company.companyType())).append(',')
                    .append(checkNull(company.companySubtype())).append(',')
                    .append(checkNull(company.dateOfCessation())).append(',')
                    .append(checkNull(company.dateOfCreation())).append(',')
                    .append(checkNull(company.dateOfCessation())).append(',')
                    .append(checkNull(company.dateOfCreation())).append(',')
                    .append(escapeReservedCsvCharacters(checkNull(company.sicCodes()))).append(',')
                    .append(escapeReservedCsvCharacters(getAddressAsString(company.registeredOfficeAddress())))
                    .append('\n');
        }
        return csv.toString();
    }

    private String checkNull(Object value) {
        return value == null ? "" : value.toString();
    }

    private String getAddressAsString(Address address) {
        if (address == null) {
            return "";
        }
        return java.util.stream.Stream.of(
                checkNull(address.addressLine1()), checkNull(address.locality()),
                checkNull(address.country()), checkNull(address.postalCode()))
                .filter(part -> !part.isEmpty())
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
