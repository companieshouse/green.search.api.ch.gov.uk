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
}
