package uk.gov.companieshouse.search.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class EricHeaderHelperTest {

    @Test
    void getIdentityReturnsHeaderValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EricHeaderHelper.ERIC_IDENTITY, "identity");

        assertEquals("identity", EricHeaderHelper.getIdentity(request));
    }

    @Test
    void getIdentityTypeReturnsHeaderValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EricHeaderHelper.ERIC_IDENTITY_TYPE, "key");

        assertEquals("key", EricHeaderHelper.getIdentityType(request));
    }

    @Test
    void missingHeaderReturnsNull() {
        assertNull(EricHeaderHelper.getIdentity(new MockHttpServletRequest()));
    }

    @Test
    void blankHeaderReturnsNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EricHeaderHelper.ERIC_IDENTITY_TYPE, " ");

        assertNull(EricHeaderHelper.getIdentityType(request));
    }
}
