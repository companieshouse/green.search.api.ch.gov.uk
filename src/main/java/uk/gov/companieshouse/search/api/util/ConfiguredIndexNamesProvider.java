package uk.gov.companieshouse.search.api.util;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.environment.EnvironmentReader;

@Component
public class ConfiguredIndexNamesProvider {

    private static final String ALPHABETICAL_SEARCH_INDEX_ENVIRONMENT_VARIABLE = "ALPHABETICAL_SEARCH_INDEX";

    private final EnvironmentReader environment;

    public ConfiguredIndexNamesProvider(EnvironmentReader environment) {
        this.environment = environment;
    }

    /**
     * @return the value of the {@link #ALPHABETICAL_SEARCH_INDEX_ENVIRONMENT_VARIABLE} environment variable
     */
    public String alphabetical() {
        return environment.getMandatoryString(ALPHABETICAL_SEARCH_INDEX_ENVIRONMENT_VARIABLE);
    }

}
