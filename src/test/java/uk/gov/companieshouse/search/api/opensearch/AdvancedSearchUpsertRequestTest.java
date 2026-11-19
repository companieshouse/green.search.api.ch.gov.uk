package uk.gov.companieshouse.search.api.opensearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.company.RegisteredOfficeAddressApi;

class AdvancedSearchUpsertRequestTest {

    @Test
    @SuppressWarnings("unchecked")
    void buildsAdvancedIndexDocument() {
        CompanyProfileApi company = new CompanyProfileApi();
        company.setType("ltd");
        company.setSubtype("community-interest-company");
        company.setCompanyNumber("12345678");
        company.setCompanyName("Example Ltd");
        company.setCompanyStatus("active");
        company.setSicCodes(new String[]{"62020"});
        company.setDateOfCreation(LocalDate.of(2020, 1, 2));
        RegisteredOfficeAddressApi address = new RegisteredOfficeAddressApi();
        address.setAddressLine1("1 Example Street");
        address.setLocality("London");
        address.setPostalCode("AB1 2CD");
        company.setRegisteredOfficeAddress(address);
        company.setLinks(Map.of("self", "/company/12345678"));

        Map<String, Object> document =
                new AdvancedSearchUpsertRequest().buildRequest(company, "example", "example");
        Map<String, Object> currentCompany = (Map<String, Object>) document.get("current_company");
        Map<String, Object> indexedAddress = (Map<String, Object>) currentCompany.get("address");
        Map<String, Object> links = (Map<String, Object>) document.get("links");

        assertEquals("ltd", document.get("company_type"));
        assertEquals("community-interest-company", document.get("company_subtype"));
        assertEquals("Example Ltd", currentCompany.get("corporate_name"));
        assertEquals("2020-01-02", currentCompany.get("date_of_creation"));
        assertEquals(List.of("62020"), currentCompany.get("sic_codes"));
        assertEquals("1 Example Street, AB1 2CD, London", currentCompany.get("full_address"));
        assertEquals("1 Example Street", indexedAddress.get("address_line_1"));
        assertEquals("/company/12345678", links.get("self"));
        assertEquals("searchresults#company", document.get("kind"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowsMissingAddressAndLinks() {
        CompanyProfileApi company = new CompanyProfileApi();
        company.setCompanyNumber("12345678");
        company.setCompanyName("Example Ltd");

        Map<String, Object> document =
                new AdvancedSearchUpsertRequest().buildRequest(company, "", "");
        Map<String, Object> currentCompany = (Map<String, Object>) document.get("current_company");

        assertNotNull(currentCompany.get("address"));
        assertEquals("", currentCompany.get("full_address"));
    }
}
