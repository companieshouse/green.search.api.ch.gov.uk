package uk.gov.companieshouse.search.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Links(
    @JsonProperty("company_profile")
    String companyProfile
) {

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
