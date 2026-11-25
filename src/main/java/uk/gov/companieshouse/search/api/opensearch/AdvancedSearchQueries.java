package uk.gov.companieshouse.search.api.opensearch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermsQuery;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;

@Component
public class AdvancedSearchQueries {

    public Query buildAdvancedSearchQuery(AdvancedSearchQueryParams params) {
        List<Query> filters = new ArrayList<>();
        List<Query> exclusions = new ArrayList<>();

        if (params.getCompanyNameIncludes() != null) {
            filters.add(MatchQuery.of(m -> m.field("current_company.corporate_name")
                    .query(FieldValue.of(params.getCompanyNameIncludes()))
                    .operator(Operator.And)).toQuery());
        }
        if (params.getLocation() != null) {
            filters.add(MatchQuery.of(m -> m.field("current_company.full_address")
                    .query(FieldValue.of(params.getLocation()))
                    .operator(Operator.And)).toQuery());
        }
        if (params.getCompanyStatusList() != null) {
            filters.add(termsQuery("current_company.company_status.keyword", params.getCompanyStatusList()));
        }
        addRangeQueryDates(filters, params.getIncorporatedFrom(), params.getIncorporatedTo(),
                "current_company.date_of_creation");
        addRangeQueryDates(filters, params.getDissolvedFrom(), params.getDissolvedTo(),
                "current_company.date_of_cessation");
        if (params.getSicCodes() != null) {
            filters.add(termsQuery("current_company.sic_codes", params.getSicCodes()));
        }
        if (params.getCompanyTypeList() != null) {
            filters.add(termsQuery("company_type", params.getCompanyTypeList()));
        }
        if (params.getCompanySubtypeList() != null) {
            filters.add(termsQuery("company_subtype", params.getCompanySubtypeList()));
        }
        if (params.getCompanyNameExcludes() != null) {
            exclusions.add(MatchQuery.of(m -> m.field("current_company.corporate_name")
                    .query(FieldValue.of(params.getCompanyNameExcludes()))
                    .operator(Operator.Or)).toQuery());
        }

        return BoolQuery.of(b -> b.filter(filters).mustNot(exclusions)).toQuery();
    }

    private Query termsQuery(String field, List<String> values) {
        List<FieldValue> terms = values.stream().map(FieldValue::of).toList();
        return TermsQuery.of(t -> t.field(field).terms(tf -> tf.value(terms))).toQuery();
    }

    private void addRangeQueryDates(List<Query> filters, LocalDate from, LocalDate to, String field) {
        if (from == null && to == null) {
            return;
        }
        filters.add(RangeQuery.of(r -> {
            r.field(field);
            if (from != null) {
                r.gte(JsonData.of(from.toString()));
            }
            if (to != null) {
                r.lte(JsonData.of(to.toString()));
            }
            return r;
        }).toQuery());
    }
}
