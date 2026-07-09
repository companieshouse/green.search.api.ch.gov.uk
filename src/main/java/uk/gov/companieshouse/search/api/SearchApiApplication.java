package uk.gov.companieshouse.search.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SearchApiApplication implements WebMvcConfigurer {

    public static final String APPLICATION_NAME_SPACE = "search.api.ch.gov.uk";

    public static void main(String[] args) {
        SpringApplication.run(SearchApiApplication.class, args);
    }
}
