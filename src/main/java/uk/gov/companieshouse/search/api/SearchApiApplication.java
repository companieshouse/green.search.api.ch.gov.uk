package uk.gov.companieshouse.search.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.search.api.interceptor.UserAuthorisationInterceptor;

@SpringBootApplication
public class SearchApiApplication implements WebMvcConfigurer {

    public static final String APPLICATION_NAME_SPACE = "green.search.api.ch.gov.uk";

    private final UserAuthorisationInterceptor authorisationInterceptor;

    public SearchApiApplication(UserAuthorisationInterceptor authorisationInterceptor) {
        this.authorisationInterceptor = authorisationInterceptor;
    }

    public static void main(String[] args) {
        SpringApplication.run(SearchApiApplication.class, args);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(authorisationInterceptor)
                .addPathPatterns("/alphabetical-search/companies/{company_number}");
    }
}
