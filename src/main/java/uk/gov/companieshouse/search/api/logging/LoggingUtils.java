package uk.gov.companieshouse.search.api.logging;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.search.api.SearchApiApplication.APPLICATION_NAME_SPACE;

public class LoggingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public static final String MESSAGE = "message";
    public static final String ORDERED_ALPHAKEY = "ordered_alphakey";
    public static final String ORDERED_ALPHAKEY_WITH_ID = "ordered_alphakey_with_id";

    private LoggingUtils() throws IllegalAccessException {
        throw new IllegalAccessException("LoggingUtils is not to be instantiated");
    }

    public static Logger getLogger() {
        return LOGGER;
    }

}
