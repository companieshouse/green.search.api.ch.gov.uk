package uk.gov.companieshouse.search.api.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class UserAuthorisationInterceptorTest {

    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static final String ERIC_KEY_ROLES = "ERIC-Authorised-Key-Roles";

    private final UserAuthorisationInterceptor interceptor = new UserAuthorisationInterceptor();
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
    }

    private MockHttpServletRequest apiKeyRequest(String method, boolean internal) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/alphabetical-search/companies/12345678");
        request.addHeader("X-Request-ID", "requestId");
        request.addHeader(ERIC_IDENTITY_TYPE, "key");
        if (internal) {
            request.addHeader(ERIC_KEY_ROLES, "*");
        }
        return request;
    }

    @Test
    void internalApiKeyCanPut() {
        assertTrue(interceptor.preHandle(apiKeyRequest("PUT", true), response, new Object()));
    }

    @Test
    void internalApiKeyCanDelete() {
        assertTrue(interceptor.preHandle(apiKeyRequest("DELETE", true), response, new Object()));
    }

    @Test
    void internalApiKeyCannotGet() {
        assertFalse(interceptor.preHandle(apiKeyRequest("GET", true), response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void nonInternalApiKeyIsUnauthorised() {
        assertFalse(interceptor.preHandle(apiKeyRequest("PUT", false), response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void unrecognisedIdentityTypeIsUnauthorised() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/alphabetical-search/companies/12345678");
        request.addHeader("X-Request-ID", "requestId");
        request.addHeader(ERIC_IDENTITY_TYPE, "oauth2");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }
}
