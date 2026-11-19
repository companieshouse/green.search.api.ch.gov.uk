package uk.gov.companieshouse.search.api.opensearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;

class AdvancedSearchQueriesTest {

    private final AdvancedSearchQueries queries = new AdvancedSearchQueries();

    @Test
    void buildsAllAdvancedSearchFilters() {
        AdvancedSearchQueryParams params = new AdvancedSearchQueryParams();
        params.setCompanyNameIncludes("Example Ltd");
        params.setLocation("London");
        params.setCompanyStatusList(List.of("active"));
        params.setIncorporatedFrom(LocalDate.of(2000, 1, 1));
        params.setIncorporatedTo(LocalDate.of(2020, 12, 31));
        params.setDissolvedFrom(LocalDate.of(2021, 1, 1));
        params.setDissolvedTo(LocalDate.of(2024, 12, 31));
        params.setSicCodes(List.of("62020"));
        params.setCompanyTypeList(List.of("ltd"));
        params.setCompanySubtypeList(List.of("community-interest-company"));
        params.setCompanyNameExcludes("Holdings");

        Query query = queries.buildAdvancedSearchQuery(params);
        assertTrue(query.isBool());
        List<Query> filters = query.bool().filter();
        assertTrue(filters.stream().anyMatch(filter -> filter.isMatch()
                && filter.match().field().equals("current_company.corporate_name")
                && filter.match().query().stringValue().equals("Example Ltd")));
        assertTrue(filters.stream().anyMatch(filter -> filter.isMatch()
                && filter.match().field().equals("current_company.full_address")));
        assertTrue(filters.stream().anyMatch(filter -> filter.isTerms()
                && filter.terms().field().equals("current_company.company_status.keyword")));
        assertTrue(filters.stream().anyMatch(filter -> filter.isTerms()
                && filter.terms().field().equals("current_company.sic_codes")));
        assertTrue(filters.stream().anyMatch(filter -> filter.isTerms()
                && filter.terms().field().equals("company_type")));
        assertTrue(filters.stream().anyMatch(filter -> filter.isTerms()
                && filter.terms().field().equals("company_subtype")));
        assertEquals(2, filters.stream().filter(Query::isRange).count());
        assertEquals(1, query.bool().mustNot().size());
        assertTrue(query.bool().mustNot().stream().anyMatch(exclusion -> exclusion.isMatch()
                && exclusion.match().query().stringValue().equals("Holdings")));
    }

    @Test
    void buildsAnEmptyBooleanQueryWithoutFilters() {
        Query query = queries.buildAdvancedSearchQuery(new AdvancedSearchQueryParams());
        assertTrue(query.isBool());
    }
}
