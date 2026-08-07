package uk.gov.companieshouse.search.api.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Company(
    @JsonProperty("company_name") String companyName,
    @JsonProperty("company_number") String companyNumber,
    @JsonProperty("company_status") String companyStatus,
    @JsonProperty("company_type") String companyType,
    @JsonProperty("company_subtype") String companySubtype,
    @JsonProperty("ordered_alpha_key_with_id") String orderedAlphaKeyWithId,
    @JsonProperty("kind") String kind,
    @JsonProperty("record_type") String recordType,
    @JsonProperty("links") Links links,
    @JsonProperty("date_of_cessation") LocalDate dateOfCessation,
    @JsonProperty("date_of_creation") LocalDate dateOfCreation,
    @JsonProperty("registered_office_address") Address registeredOfficeAddress,
    @JsonProperty("previous_company_names") List<PreviousCompanyName> previousCompanyNames,
    @JsonProperty("matched_previous_company_name") PreviousCompanyName matchedPreviousCompanyName,
    @JsonProperty("sic_codes") List<String> sicCodes
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String companyName;
        private String companyNumber;
        private String companyStatus;
        private String companyType;
        private String companySubtype;
        private String orderedAlphaKeyWithId;
        private String kind;
        private String recordType;
        private Links links;
        private LocalDate dateOfCessation;
        private LocalDate dateOfCreation;
        private Address registeredOfficeAddress;
        private List<PreviousCompanyName> previousCompanyNames;
        private PreviousCompanyName matchedPreviousCompanyName;
        private List<String> sicCodes;

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder companyStatus(String companyStatus) {
            this.companyStatus = companyStatus;
            return this;
        }

        public Builder companyType(String companyType) {
            this.companyType = companyType;
            return this;
        }

        public Builder companySubtype(String companySubtype) {
            this.companySubtype = companySubtype;
            return this;
        }

        public Builder orderedAlphaKeyWithId(String orderedAlphaKeyWithId) {
            this.orderedAlphaKeyWithId = orderedAlphaKeyWithId;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder recordType(String recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder links(Links links) {
            this.links = links;
            return this;
        }

        public Builder dateOfCessation(LocalDate dateOfCessation) {
            this.dateOfCessation = dateOfCessation;
            return this;
        }

        public Builder dateOfCreation(LocalDate dateOfCreation) {
            this.dateOfCreation = dateOfCreation;
            return this;
        }

        public Builder registeredOfficeAddress(Address registeredOfficeAddress) {
            this.registeredOfficeAddress = registeredOfficeAddress;
            return this;
        }

        public Builder previousCompanyNames(List<PreviousCompanyName> previousCompanyNames) {
            this.previousCompanyNames = previousCompanyNames;
            return this;
        }

        public Builder matchedPreviousCompanyName(PreviousCompanyName matchedPreviousCompanyName) {
            this.matchedPreviousCompanyName = matchedPreviousCompanyName;
            return this;
        }

        public Builder sicCodes(List<String> sicCodes) {
            this.sicCodes = sicCodes;
            return this;
        }

        public Company build() {
            return new Company(companyName, companyNumber, companyStatus, companyType,
                    companySubtype, orderedAlphaKeyWithId, kind, recordType, links,
                    dateOfCessation, dateOfCreation, registeredOfficeAddress,
                    previousCompanyNames, matchedPreviousCompanyName, sicCodes);
        }
    }
}
