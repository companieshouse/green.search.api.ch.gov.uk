package uk.gov.companieshouse.search.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.search.api.exception.DateFormatException;
import uk.gov.companieshouse.search.api.exception.MappingException;
import uk.gov.companieshouse.search.api.exception.SizeException;
import uk.gov.companieshouse.search.api.model.AdvancedSearchQueryParams;

@ExtendWith(MockitoExtension.class)
class AdvancedQueryParamMapperTest {

    @Mock
    private EnvironmentReader environmentReader;

    @InjectMocks
    private AdvancedQueryParamMapper mapper;

    @Test
    void mapsSearchParametersAndDefaultsPagination() throws Exception {
        when(environmentReader.getMandatoryInteger("ADVANCED_SEARCH_DEFAULT_SIZE")).thenReturn(20);
        when(environmentReader.getMandatoryInteger("ADVANCED_SEARCH_MAX_SIZE")).thenReturn(500);

        AdvancedSearchQueryParams params = mapper.mapAdvancedQueryParameters(
                null, "Example Ltd", "London", "2000-01-02", null, List.of("ACTIVE"),
                List.of("62020"), List.of("LTD"), List.of("LP"), null, "2024-12-31", null, null);

        assertEquals(0, params.getStartIndex());
        assertEquals(20, params.getSize());
        assertEquals("active", params.getCompanyStatusList().getFirst());
        assertEquals("ltd", params.getCompanyTypeList().getFirst());
        assertEquals("lp", params.getCompanySubtypeList().getFirst());
        assertEquals(LocalDate.of(2000, 1, 2), params.getIncorporatedFrom());
        assertEquals(LocalDate.of(2024, 12, 31), params.getDissolvedTo());
    }

    @Test
    void rejectsInvalidDate() {
        assertThrows(DateFormatException.class, () -> mapper.mapAdvancedQueryParameters(
                0, null, null, "31-12-2024", null, null, null, null, null, null, null, null, 20));
    }

    @Test
    void rejectsUnsupportedCompanyStatus() {
        assertThrows(MappingException.class, () -> mapper.mapAdvancedQueryParameters(
                0, null, null, null, null, List.of("unknown"), null, null, null, null, null, null, 20));
    }

    @Test
    void rejectsSizeOutsideConfiguredRange() {
        when(environmentReader.getMandatoryInteger("ADVANCED_SEARCH_DEFAULT_SIZE")).thenReturn(20);
        when(environmentReader.getMandatoryInteger("ADVANCED_SEARCH_MAX_SIZE")).thenReturn(500);

        assertThrows(SizeException.class, () -> mapper.mapAdvancedQueryParameters(
                0, null, null, null, null, null, null, null, null, null, null, null, 501));
    }
}
