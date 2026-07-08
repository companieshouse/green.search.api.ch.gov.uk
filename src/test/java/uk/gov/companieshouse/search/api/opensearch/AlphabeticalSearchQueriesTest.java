package uk.gov.companieshouse.search.api.opensearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import static org.junit.jupiter.api.Assertions.*;

class AlphabeticalSearchQueriesTest {

    private AlphabeticalSearchQueries queries;

    @BeforeEach
    void setUp() {
        queries = new AlphabeticalSearchQueries();
    }

    @Test
    @DisplayName("Should create ordered alpha key search query")
    void createOrderedAlphaKeySearchQuery() {

        String orderedAlphaKey = "orderAlphaKey";

        Query query = queries.createOrderedAlphaKeySearchQuery(orderedAlphaKey);

        assertAll(
                () -> assertNotNull(query),
                () -> assertTrue(query.isMatch()),
                () -> assertEquals("items.ordered_alpha_key", query.match().field()),
                () -> assertEquals(orderedAlphaKey, query.match().query().stringValue())
        );
    }

    @Test
    @DisplayName("Should create ordered alpha key keyword prefix query")
    void createOrderedAlphaKeyKeywordQuery() {

        String orderedAlphaKey = "orderAlphaKey";

        Query query = queries.createOrderedAlphaKeyKeywordQuery(orderedAlphaKey);

        assertAll(
                () -> assertNotNull(query),
                () -> assertTrue(query.isPrefix()),
                () -> assertEquals("items.ordered_alpha_key.keyword", query.prefix().field()),
                () -> assertEquals(orderedAlphaKey, query.prefix().value())
        );
    }

    @Test
    @DisplayName("Should create starts with match phrase prefix query")
    void createStartsWithQuery() {

        String corporateName = "TEST";

        Query query = queries.createStartsWithQuery(corporateName);

        assertAll(
                () -> assertNotNull(query),
                () -> assertTrue(query.isMatchPhrasePrefix()),
                () -> assertEquals("items.corporate_name.startswith", query.matchPhrasePrefix().field()),
                () -> assertEquals(corporateName, query.matchPhrasePrefix().query())
        );
    }
}
