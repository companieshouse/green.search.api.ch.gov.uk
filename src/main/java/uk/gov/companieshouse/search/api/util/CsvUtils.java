package uk.gov.companieshouse.search.api.util;

public final class CsvUtils {

    private CsvUtils() {}

    public static String escapeReservedCsvCharacters(String value) {
        if (value == null) {
            return "";
        }
        String normalised = value.replace("\r", "").replace("\n", "");
        if (normalised.contains("\"")) {
            normalised = normalised.replace("\"", "\"\"");
        }
        if (normalised.contains(",") || normalised.contains("\"")) {
            return "\"" + normalised + "\"";
        }
        return normalised;
    }
}
