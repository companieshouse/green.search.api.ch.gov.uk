package uk.gov.companieshouse.search.api;

import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class SearchApiApplicationTests {

	@MockitoBean
	private OpenSearchClient alphabeticalOpenSearchClient;

	@Test
	void contextLoads() {
	}

}
