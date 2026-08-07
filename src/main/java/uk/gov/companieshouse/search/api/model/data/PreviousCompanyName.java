package uk.gov.companieshouse.search.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"ordered_alpha_key"})
public record PreviousCompanyName(
    @JsonProperty("ceased_on") LocalDate dateOfNameCessation,
    @JsonProperty("effective_from") LocalDate dateOfNameEffectiveness,
    @JsonProperty("name") String name,
    @JsonProperty("company_number") String companyNumber
) {}
