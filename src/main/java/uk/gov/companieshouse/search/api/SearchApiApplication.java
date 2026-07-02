package uk.gov.companieshouse.search.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SearchApiApplication {

	public static final String APPLICATION_NAME_SPACE = "green.search.api.ch.gov.uk";


	public static void main(String[] args) {
		SpringApplication.run(SearchApiApplication.class, args);
	}

}
