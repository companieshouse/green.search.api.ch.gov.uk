# green.search.api.ch.gov.uk
The green deployment version of the search-api

## Advanced search

The advanced-search endpoint queries the OpenSearch index configured by
`ADVANCED_SEARCH_INDEX` at `ADVANCED_SEARCH_URL`:

| Method | Path | Purpose |
|---|---|---|
| GET | `/green/advanced-search/companies` | Search companies and return JSON results |
| GET | `/green/advanced-search/csv` | Return the same search results as CSV |
| PUT | `/green/advanced-search/companies/{company_number}` | Insert or update a company in the advanced index |
| DELETE | `/green/advanced-search/companies/{company_number}` | Delete a company from the advanced index |

Search requests accept `company_name_includes`, `company_name_excludes`, `location`,
`incorporated_from`, `incorporated_to`, `dissolved_from`, `dissolved_to`, `company_status`,
`sic_codes`, `company_type` and `company_subtype`. Pagination uses `start_index` and `size`.
Dates must use `yyyy-MM-dd`; status, type and subtype values are validated against the
accepted Companies House values.

The advanced index also requires `ADVANCED_SEARCH_DEFAULT_SIZE`, `ADVANCED_SEARCH_MAX_SIZE`
and `ALPHAKEY_SERVICE_URL`. Set `USE_AWS_SIGV4=true` when connecting to an AWS OpenSearch
domain that requires IAM authentication.
