package uk.gov.companieshouse.search.api.opensearch;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhrasePrefixQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.PrefixQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.search.api.logging.LoggingUtils;

@Component
public class AlphabeticalSearchQueries extends AbstractSearchQuery {

    public Query createOrderedAlphaKeySearchQuery(String orderedAlphaKey) {

        LoggingUtils.getLogger().info("Creating Ordered Alpha Key Search Query");

        return MatchQuery.of(builder -> builder
                .field("items.ordered_alpha_key")
                .query(FieldValue.of(orderedAlphaKey))
        ).toQuery();
    }

    public Query createOrderedAlphaKeyKeywordQuery(String orderedAlphaKey) {

        LoggingUtils.getLogger().info("Creating Ordered Alpha Key Keyword Query for OpenSearch");

        return PrefixQuery.of(builder -> builder
                .field("items.ordered_alpha_key.keyword")
                .value(orderedAlphaKey)
        ).toQuery();
    }

    public Query createStartsWithQuery(String corporateName) {

        LoggingUtils.getLogger().info("Creating Starts With Query for OpenSearch");

        return MatchPhrasePrefixQuery.of(builder -> builder
                .field("items.corporate_name.startswith")
                .query(corporateName)
        ).toQuery();
    }
}
