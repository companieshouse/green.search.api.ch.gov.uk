package uk.gov.companieshouse.search.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Address(
        @JsonProperty("address_line_1") String addressLine1,
        @JsonProperty("address_line_2") String addressLine2,
        @JsonProperty("locality") String locality,
        @JsonProperty("postal_code") String postalCode,
        @JsonProperty("premises") String premises,
        @JsonProperty("region") String region,
        @JsonProperty("country") String country
) {

}
