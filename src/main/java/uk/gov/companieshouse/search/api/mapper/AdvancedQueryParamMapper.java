package uk.gov.companieshouse.search.api.mapper;

import static uk.gov.companieshouse.search.api.logging.LoggingUtils.COMPANY_STATUS;
import static uk.gov.companieshouse.search.api.logging.LoggingUtils.COMPANY_SUBTYPE;
import static uk.gov.companieshouse.search.api.logging.LoggingUtils.COMPANY_TYPE;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.search.api.exception.DateFormatException;
import uk.gov.companieshouse.search.api.exception.MappingException;
import uk.gov.companieshouse.search.api.exception.SizeException;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;
import uk.gov.companieshouse.search.api.service.search.SearchRequestUtils;

@Component
public class AdvancedQueryParamMapper {

    private static final List<String> ACCEPTED_COMPANY_STATUS = Arrays.asList(
            "active", "dissolved", "open", "closed", "removed", "converted-closed",
            "receivership", "administration", "liquidation", "insolvency-proceedings",
            "voluntary-arrangement");
    private static final List<String> ACCEPTED_COMPANY_TYPES = Arrays.asList(
            "private-unlimited", "ltd", "plc", "old-public-company",
            "private-limited-guarant-nsc-limited-exemption", "limited-partnership",
            "private-limited-guarant-nsc", "converted-or-closed", "private-unlimited-nsc",
            "private-limited-shares-section-30-exemption", "protected-cell-company",
            "assurance-company", "oversea-company", "eeig-establishment", "icvc-securities",
            "icvc-warrant", "icvc-umbrella", "registered-society-non-jurisdictional",
            "industrial-and-provident-society", "northern-ireland", "northern-ireland-other",
            "llp", "royal-charter", "investment-company-with-variable-capital",
            "unregistered-company", "other", "european-public-limited-liability-company-se",
            "united-kingdom-societas", "uk-establishment", "scottish-partnership",
            "charitable-incorporated-organisation", "scottish-charitable-incorporated-organisation",
            "further-education-or-sixth-form-college-corporation", "eeig",
            "registered-overseas-entity", "ukeig");
    private static final List<String> ACCEPTED_COMPANY_SUBTYPES = Arrays.asList(
            "community-interest-company", "private-fund-limited-partnership",
            "lp", "pflp", "slp", "spflp");

    private static final String ADVANCED_SEARCH_DEFAULT_SIZE = "ADVANCED_SEARCH_DEFAULT_SIZE";
    private static final String ADVANCED_SEARCH_MAX_SIZE = "ADVANCED_SEARCH_MAX_SIZE";

    private final EnvironmentReader environmentReader;

    public AdvancedQueryParamMapper(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    public AdvancedSearchQueryParams mapAdvancedQueryParameters(
            Integer startIndex, String companyNameIncludes, String location,
            String incorporatedFrom, String incorporatedTo, List<String> companyStatusList,
            List<String> sicCodes, List<String> companyTypeList, List<String> companySubtypeList,
            String dissolvedFrom, String dissolvedTo, String companyNameExcludes, Integer size)
            throws DateFormatException, MappingException, SizeException {

        AdvancedSearchQueryParams params = new AdvancedSearchQueryParams();
        params.setStartIndex(startIndex == null || startIndex < 0 ? 0 : startIndex);
        params.setCompanyNameIncludes(companyNameIncludes);
        params.setLocation(location);
        params.setSicCodes(sicCodes);
        params.setCompanyNameExcludes(companyNameExcludes);
        mapDates(params, incorporatedFrom, incorporatedTo, dissolvedFrom, dissolvedTo);

        if (companyStatusList != null) {
            params.setCompanyStatusList(mapListParam(companyStatusList, ACCEPTED_COMPANY_STATUS, COMPANY_STATUS));
        }
        if (companyTypeList != null) {
            params.setCompanyTypeList(mapListParam(companyTypeList, ACCEPTED_COMPANY_TYPES, COMPANY_TYPE));
        }
        if (companySubtypeList != null) {
            params.setCompanySubtypeList(mapListParam(companySubtypeList, ACCEPTED_COMPANY_SUBTYPES, COMPANY_SUBTYPE));
        }

        params.setSize(SearchRequestUtils.checkResultsSize(size,
                environmentReader.getMandatoryInteger(ADVANCED_SEARCH_DEFAULT_SIZE),
                environmentReader.getMandatoryInteger(ADVANCED_SEARCH_MAX_SIZE)));
        return params;
    }

    private void mapDates(AdvancedSearchQueryParams params, String incorporatedFrom,
                          String incorporatedTo, String dissolvedFrom, String dissolvedTo)
            throws DateFormatException {
        try {
            if (incorporatedFrom != null) {
                params.setIncorporatedFrom(LocalDate.parse(incorporatedFrom));
            }
            if (incorporatedTo != null) {
                params.setIncorporatedTo(LocalDate.parse(incorporatedTo));
            }
            if (dissolvedFrom != null) {
                params.setDissolvedFrom(LocalDate.parse(dissolvedFrom));
            }
            if (dissolvedTo != null) {
                params.setDissolvedTo(LocalDate.parse(dissolvedTo));
            }
        } catch (DateTimeParseException e) {
            throw new DateFormatException("Date fields must use yyyy-MM-dd");
        }
    }

    private List<String> mapListParam(List<String> values, List<String> acceptedValues, String field)
            throws MappingException {
        List<String> mappedValues = new ArrayList<>();
        for (String value : values) {
            String normalised = value.toLowerCase();
            if (!acceptedValues.contains(normalised)) {
                throw new MappingException("failed to map value for " + field + ": " + value);
            }
            mappedValues.add(normalised);
        }
        return mappedValues;
    }
}
