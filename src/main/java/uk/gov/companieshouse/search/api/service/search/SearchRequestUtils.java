package uk.gov.companieshouse.search.api.service.search;

import uk.gov.companieshouse.search.api.exception.SizeException;

public class SearchRequestUtils {

    public static Integer checkResultsSize(Integer size, Integer defaultSize, Integer maxSize) throws SizeException {

        if (size == null) {
            size = defaultSize;
        }

        if (size <= 0 || size > maxSize) {
            throw new SizeException("Size parameter is less than or equal to 0 or greater than maximum");
        }

        return size;
    }
}
