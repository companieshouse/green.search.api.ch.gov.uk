package uk.gov.companieshouse.search.api.mapper;

import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.search.api.model.TopHit;
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

    public TopHit mapAlphabeticalTopHit(Company company) {
        TopHit topHit = new TopHit();

        topHit.setCompanyName(company.getCompanyName());
        topHit.setCompanyNumber(company.getCompanyNumber());
        topHit.setCompanyStatus(company.getCompanyStatus());
        topHit.setCompanyType(company.getCompanyType());
        topHit.setLinks(company.getLinks());
        topHit.setOrderedAlphaKeyWithId(company.getOrderedAlphaKeyWithId());
        topHit.setKind(SEARCH_RESULTS_ALPHABETICAL_KIND);

        return topHit;
    }

    public Company mapAlphabeticalResponse(Hit<Object> hit) {
        Map<String, Object> sourceAsMap = (Map<String, Object>) hit.source();
        Map<String, Object> items = (Map<String, Object>) sourceAsMap.get(ITEMS_KEY);
        Map<String, Object> links = (Map<String, Object>) sourceAsMap.get(LINKS_KEY);

        Company company = new Company();
        Links companyLinks = new Links();

        company.setCompanyName((String) (items.get(CORPORATE_NAME_KEY)));
        company.setCompanyNumber((String) (items.get(COMPANY_NUMBER_KEY)));
        company.setCompanyStatus((String) (items.get(COMPANY_STATUS_KEY)));
        company.setOrderedAlphaKeyWithId((String) sourceAsMap.get(ORDERED_ALPHA_KEY_WITH_ID));
        company.setKind(SEARCH_RESULTS_ALPHABETICAL_KIND);

        companyLinks.setCompanyProfile((String) (links.get(SELF_KEY)));
        company.setLinks(companyLinks);

        company.setCompanyType((String) sourceAsMap.get(COMPANY_TYPE_KEY));

        return company;
    }
}
