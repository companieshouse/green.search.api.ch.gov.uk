package uk.gov.companieshouse.search.api.opensearch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.company.RegisteredOfficeAddressApi;

@Component
public class AdvancedSearchUpsertRequest {

    public Map<String, Object> buildRequest(CompanyProfileApi company, String orderedAlphaKey, String sameAsKey) {
        RegisteredOfficeAddressApi address = company.getRegisteredOfficeAddress();
        Map<String, Object> addressFields = addressMap(address);
        String fullAddress = createFullAddress(address);
        Map<String, Object> currentCompany = new LinkedHashMap<>();
        currentCompany.put("corporate_name", company.getCompanyName());
        currentCompany.put("sic_codes", sicCodes(company.getSicCodes()));
        currentCompany.put("company_number", company.getCompanyNumber());
        currentCompany.put("company_status", company.getCompanyStatus());
        currentCompany.put("address", addressFields);
        currentCompany.put("wildcard_key", orderedAlphaKey);
        currentCompany.put("date_of_creation", dateString(company.getDateOfCreation()));
        if (company.getDateOfCessation() != null) {
            currentCompany.put("date_of_cessation", dateString(company.getDateOfCessation()));
        }
        currentCompany.put("full_address", fullAddress);
        currentCompany.put("record_type", "companies");
        currentCompany.put("same_as_key", sameAsKey);

        Map<String, Object> items = new LinkedHashMap<>(currentCompany);
        items.put("address", addressFields);

        Map<String, Object> links = new LinkedHashMap<>();
        Map<String, String> companyLinks = company.getLinks();
        links.put("self", companyLinks == null ? null : companyLinks.get("self"));

        Map<String, Object> source = new LinkedHashMap<>();
        source.put("company_type", company.getType());
        source.put("company_subtype", company.getSubtype());
        source.put("current_company", currentCompany);
        source.put("items", items);
        source.put("kind", "searchresults#company");
        source.put("links", links);
        source.put("sort_key", orderedAlphaKey + "0");
        return source;
    }

    private Map<String, Object> addressMap(RegisteredOfficeAddressApi address) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("premises", address == null ? null : address.getPremises());
        fields.put("address_line_1", address == null ? null : address.getAddressLine1());
        fields.put("address_line_2", address == null ? null : address.getAddressLine2());
        fields.put("postal_code", address == null ? null : address.getPostalCode());
        fields.put("locality", address == null ? null : address.getLocality());
        fields.put("region", address == null ? null : address.getRegion());
        fields.put("country", address == null ? null : address.getCountry());
        return fields;
    }

    private String createFullAddress(RegisteredOfficeAddressApi address) {
        if (address == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        addIfPresent(parts, address.getPremises());
        addIfPresent(parts, address.getAddressLine1());
        addIfPresent(parts, address.getAddressLine2());
        addIfPresent(parts, address.getPostalCode());
        addIfPresent(parts, address.getLocality());
        addIfPresent(parts, address.getRegion());
        addIfPresent(parts, address.getCountry());
        return String.join(", ", parts);
    }

    private void addIfPresent(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value);
        }
    }

    private List<String> sicCodes(String[] sicCodes) {
        return sicCodes == null ? null : List.of(sicCodes);
    }

    private String dateString(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
