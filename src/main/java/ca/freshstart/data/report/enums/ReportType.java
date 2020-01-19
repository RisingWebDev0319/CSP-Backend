package ca.freshstart.data.report.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ReportType {
    @JsonProperty("clients")
    CLIENTS("clients"),
    @JsonProperty("rooms")
    ROOMS("rooms"),
    @JsonProperty("therapists")
    THERAPISTS("therapists");

    private final String value;

    /**
     * @param text
     */
    ReportType(final String text) {
        this.value = text;
    }

    @Override
    public String toString() {
        return this.value.toLowerCase();
    }
}
