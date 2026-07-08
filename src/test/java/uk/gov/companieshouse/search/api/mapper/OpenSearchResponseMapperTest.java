package uk.gov.companieshouse.search.api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.TotalHits;
import org.opensearch.client.opensearch.core.search.TotalHitsRelation;
import uk.gov.companieshouse.search.api.model.TopHit;
import uk.gov.companieshouse.search.api.model.data.Company;
import uk.gov.companieshouse.search.api.model.data.Links;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.companieshouse.search.api.mapper.OpenSearchResponseMapper.SEARCH_RESULTS_ALPHABETICAL_KIND;

@ExtendWith(MockitoExtension.class)
class OpenSearchResponseMapperTest {

    private static final String COMPANY_NAME = "TEST COMPANY";
    private static final String COMPANY_NUMBER = "00000000";
    private static final String COMPANY_STATUS_ACTIVE = "active";
    private static final String COMPANY_TYPE = "ltd";
    private static final String COMPANY_PROFILE_LINK = "/company/00000000";
    private static final String ORDERED_ALPHA_KEY_WITH_ID = "ordered_alpha_key_with_id";

    @InjectMocks
    private OpenSearchResponseMapper mapper;

    @Test
    @DisplayName("Map alphabetical response successful")
    void mapAlphabeticalResponseTest() throws IOException, URISyntaxException {

        String alphabeticalResponseJsonFile = "alphabetical_search_response.json";
        String alphabeticalSearchJsonResponse = readFile(alphabeticalResponseJsonFile);
        HitsMetadata<Object> searchHits = createHits(alphabeticalSearchJsonResponse);

        Company company =
                mapper.mapAlphabeticalResponse(searchHits.hits().getFirst());

        assertEquals(COMPANY_NAME, company.getCompanyName());
        assertEquals(COMPANY_NUMBER, company.getCompanyNumber());
        assertEquals(COMPANY_STATUS_ACTIVE, company.getCompanyStatus());
        assertEquals(COMPANY_TYPE, company.getCompanyType());
        assertEquals(ORDERED_ALPHA_KEY_WITH_ID, company.getOrderedAlphaKeyWithId());
        assertEquals(COMPANY_PROFILE_LINK, company.getLinks().getCompanyProfile());
    }

    @Test
    @DisplayName("Map alphabetical top hit successful")
    void mapAlphabeticalTopHitSuccessful() {

        TopHit topHit = mapper.mapAlphabeticalTopHit(createAlphabeticalCompany());

        assertEquals(COMPANY_NAME, topHit.getCompanyName());
        assertEquals(COMPANY_NUMBER, topHit.getCompanyNumber());
        assertEquals(COMPANY_STATUS_ACTIVE, topHit.getCompanyStatus());
        assertEquals(COMPANY_TYPE, topHit.getCompanyType());
        assertEquals(ORDERED_ALPHA_KEY_WITH_ID, topHit.getOrderedAlphaKeyWithId());
        assertEquals(COMPANY_PROFILE_LINK, topHit.getLinks().getCompanyProfile());
        assertEquals(SEARCH_RESULTS_ALPHABETICAL_KIND, topHit.getKind());
    }

    private HitsMetadata<Object> createHits(String json) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> source = objectMapper.readValue(json, new TypeReference<>() {
        });

        Hit<Object> hit = Hit.of(h -> h
                .id("1")
                .source(source));

        return HitsMetadata.of(h -> h
                .hits(List.of(hit))
                .total(TotalHits.of(t -> t
                        .value(1)
                        .relation(TotalHitsRelation.Gte))));
    }

    private Company createAlphabeticalCompany() {
        Company company = new Company();
        company.setCompanyName(COMPANY_NAME);
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyStatus(COMPANY_STATUS_ACTIVE);
        company.setCompanyType(COMPANY_TYPE);
        company.setOrderedAlphaKeyWithId(ORDERED_ALPHA_KEY_WITH_ID);

        Links links = new Links();
        links.setCompanyProfile(COMPANY_PROFILE_LINK);
        company.setLinks(links);

        return company;
    }

    private String readFile(String fileName) throws IOException, URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).toURI());
        return new String(Files.readAllBytes(path));
    }

}

