package uk.gov.companieshouse.search.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record SearchResults<T>(
        @JsonProperty("etag") String etag,
        @JsonProperty("top_hit") TopHit topHit,
        @JsonProperty("items") List<T> items,
        @JsonProperty("kind") String kind,
        @JsonProperty("hits") Long hits
) {
    public SearchResults() {
        this(null, null, null, null, null);
    }

    public SearchResults(String etag, TopHit topHit, List<T> items, String kind) {
        this(etag, topHit, items, kind, null);
    }

}
