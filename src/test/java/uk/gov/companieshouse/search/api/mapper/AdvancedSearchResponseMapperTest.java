package uk.gov.companieshouse.search.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.core.search.Hit;
import uk.gov.companieshouse.search.api.model.TopHit;
import uk.gov.companieshouse.search.api.model.data.Company;

class AdvancedSearchResponseMapperTest {

    private final OpenSearchResponseMapper mapper = new OpenSearchResponseMapper();

    @Test
    void mapsAdvancedSearchHitAndTopHit() {
        Map<String, Object> source = Map.of(
                "company_type", "ltd",
                "company_subtype", "community-interest-company",
                "kind", "searchresults#company",
                "links", Map.of("self", "/company/12345678"),
                "current_company", Map.of(
                        "corporate_name", "Example Ltd",
                        "company_number", "12345678",
                        "company_status", "dissolved",
                        "date_of_creation", "2000-01-02",
                        "date_of_cessation", "2020-03-04",
                        "sic_codes", List.of("62020"),
                        "address", Map.of(
                                "address_line_1", "1 Example Street",
                                "locality", "London",
                                "postal_code", "AB1 2CD")));
        Hit<Object> hit = Hit.of(h -> h.id("12345678").source(source));

        Company company = mapper.mapAdvancedSearchResponse(hit);
        TopHit topHit = mapper.mapAdvancedTopHit(company);

        assertEquals("Example Ltd", company.companyName());
        assertEquals("12345678", company.companyNumber());
        assertEquals("dissolved", company.companyStatus());
        assertEquals("ltd", company.companyType());
        assertEquals("community-interest-company", company.companySubtype());
        assertEquals("search-results#company", company.kind());
        assertEquals("2020-03-04", company.dateOfCessation().toString());
        assertEquals("2000-01-02", company.dateOfCreation().toString());
        assertEquals("1 Example Street", company.registeredOfficeAddress().addressLine1());
        assertEquals("/company/12345678", topHit.getLinks().companyProfile());
        assertEquals("Example Ltd", topHit.getCompanyName());
    }

    @Test
    void doesNotExposeCessationDateForActiveCompany() {
        Map<String, Object> source = Map.of(
                "current_company", Map.of(
                        "corporate_name", "Example Ltd",
                        "company_status", "active",
                        "date_of_cessation", "2020-03-04"));
        Hit<Object> hit = Hit.of(h -> h.id("12345678").source(source));

        assertEquals(null, mapper.mapAdvancedSearchResponse(hit).dateOfCessation());
    }
}
