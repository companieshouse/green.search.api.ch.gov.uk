package uk.gov.companieshouse.search.api.mapper;

import java.time.LocalDate;
import java.util.List;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.search.api.model.TopHit;
import uk.gov.companieshouse.search.api.model.data.Address;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.data.Links;

import java.util.Map;

@Component
public class OpenSearchResponseMapper {

    private static final String CORPORATE_NAME_KEY = "corporate_name";
    private static final String COMPANY_NUMBER_KEY = "company_number";
    private static final String COMPANY_STATUS_KEY = "company_status";
    private static final String COMPANY_TYPE_KEY = "company_type";
    private static final String ITEMS_KEY = "items";
    private static final String LINKS_KEY = "links";
    private static final String SELF_KEY = "self";
    private static final String ORDERED_ALPHA_KEY_WITH_ID = "ordered_alpha_key_with_id";
    protected static final String SEARCH_RESULTS_ALPHABETICAL_KIND = "searchresults#alphabetical-search";
    private static final String SEARCH_RESULTS_COMPANY_KIND = "search-results#company";
    private static final List<String> DISSOLVED_STATUSES =
            List.of("dissolved", "closed", "converted-closed", "removed");

    public TopHit mapAlphabeticalTopHit(Company company) {
        TopHit topHit = new TopHit();

        topHit.setCompanyName(company.companyName());
        topHit.setCompanyNumber(company.companyNumber());
        topHit.setCompanyStatus(company.companyStatus());
        topHit.setCompanyType(company.companyType());
        topHit.setLinks(company.links());
        topHit.setOrderedAlphaKeyWithId(company.orderedAlphaKeyWithId());
        topHit.setKind(SEARCH_RESULTS_ALPHABETICAL_KIND);

        return topHit;
    }

    public Company mapAlphabeticalResponse(Hit<Object> hit) {
        Map<String, Object> sourceAsMap = (Map<String, Object>) hit.source();
        Map<String, Object> items = (Map<String, Object>) sourceAsMap.get(ITEMS_KEY);
        Map<String, Object> links = (Map<String, Object>) sourceAsMap.get(LINKS_KEY);

        Links companyLinks = new Links((String) (links.get(SELF_KEY)));

        return Company.builder()
                .companyName((String) items.get(CORPORATE_NAME_KEY))
                .companyNumber((String) items.get(COMPANY_NUMBER_KEY))
                .companyStatus((String) items.get(COMPANY_STATUS_KEY))
                .companyType((String) sourceAsMap.get(COMPANY_TYPE_KEY))
                .orderedAlphaKeyWithId((String) sourceAsMap.get(ORDERED_ALPHA_KEY_WITH_ID))
                .kind(SEARCH_RESULTS_ALPHABETICAL_KIND)
                .links(companyLinks)
                .build();
    }

    public TopHit mapAdvancedTopHit(Company company) {
        TopHit topHit = new TopHit();
        topHit.setCompanyName(company.companyName());
        topHit.setCompanyNumber(company.companyNumber());
        topHit.setCompanyStatus(company.companyStatus());
        topHit.setCompanyType(company.companyType());
        topHit.setCompanySubtype(company.companySubtype());
        topHit.setKind(company.kind());
        topHit.setRegisteredOfficeAddress(company.registeredOfficeAddress());
        topHit.setDateOfCessation(company.dateOfCessation());
        topHit.setDateOfCreation(company.dateOfCreation());
        topHit.setSicCodes(company.sicCodes());
        topHit.setLinks(company.links());
        return topHit;
    }

    @SuppressWarnings("unchecked")
    public Company mapAdvancedSearchResponse(Hit<Object> hit) {
        Map<String, Object> source = (Map<String, Object>) hit.source();
        Map<String, Object> currentCompany = (Map<String, Object>) source.get("current_company");
        Map<String, Object> links = (Map<String, Object>) source.get(LINKS_KEY);
        Map<String, Object> address = (Map<String, Object>) currentCompany.get("address");
        String status = (String) currentCompany.get(COMPANY_STATUS_KEY);

        LocalDate dateOfCessation = null;
        Object cessationDate = currentCompany.get("date_of_cessation");
        if (DISSOLVED_STATUSES.contains(status) && cessationDate instanceof String date) {
            dateOfCessation = LocalDate.parse(date);
        }

        LocalDate dateOfCreation = null;
        Object creationDate = currentCompany.get("date_of_creation");
        if (creationDate instanceof String date) {
            dateOfCreation = LocalDate.parse(date);
        }

        List<String> sicCodes = (List<String>) currentCompany.get("sic_codes");
        Address registeredOfficeAddress = mapAddress(address);
        Links companyLinks = links == null ? null : new Links((String) links.get(SELF_KEY));

        return Company.builder()
                .companyName((String) currentCompany.get(CORPORATE_NAME_KEY))
                .companyNumber((String) currentCompany.get(COMPANY_NUMBER_KEY))
                .companyStatus(status)
                .companyType((String) source.get(COMPANY_TYPE_KEY))
                .companySubtype((String) source.get("company_subtype"))
                .kind(SEARCH_RESULTS_COMPANY_KIND)
                .dateOfCessation(dateOfCessation)
                .dateOfCreation(dateOfCreation)
                .sicCodes(sicCodes)
                .registeredOfficeAddress(registeredOfficeAddress)
                .links(companyLinks)
                .build();
    }

    private Address mapAddress(Map<String, Object> fields) {
        if (fields == null) {
            return null;
        }
        return new Address(
                (String) fields.get("address_line_1"),
                (String) fields.get("address_line_2"),
                (String) fields.get("locality"),
                (String) fields.get("postal_code"),
                (String) fields.get("premises"),
                (String) fields.get("region"),
                (String) fields.get("country"));
    }
}
