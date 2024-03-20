package org.jembi.jempi.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkConfig {
    private Boolean reporting;
    private Integer computing;
    private Double leftMargin;
    private Double threshold;
    private Double rightMargin;
    private Double windowSize;

    public LinkConfig() {
        this(null, null, null, null, null, null);
    }

    public LinkConfig(final Boolean reporting, final Integer computing, final Double leftMargin, final Double threshold,
            final Double rightMargin,
            final Double windowSize) {
        this.reporting = reporting;
        this.computing = computing;
        this.leftMargin = leftMargin;
        this.threshold = threshold;
        this.rightMargin = rightMargin;
        this.windowSize = windowSize;
    }

    public final Boolean getReporting() {
        return reporting;
    }

    public final Integer getComputing() {
        return computing;
    }

    public final Double getLeftMargin() {
        return leftMargin;
    }

    public final Double getThreshold() {
        return threshold;
    }

    public final Double getRightMargin() {
        return rightMargin;
    }

    public final Double getWindowSize() {
        return windowSize;
    }

    public static LinkConfig fromString(final String str) {
        LinkConfig config = new LinkConfig();
        String[] pairs = str.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            switch (key) {
                case "reporting":
                    config.reporting = Boolean.parseBoolean(value);
                    break;
                case "computing":
                    config.computing = Integer.parseInt(value);
                    break;
                case "leftMargin":
                    config.leftMargin = Double.parseDouble(value);
                    break;
                case "threshold":
                    config.threshold = Double.parseDouble(value);
                    break;
                case "rightMargin":
                    config.rightMargin = Double.parseDouble(value);
                    break;
                case "windowSize":
                    config.windowSize = Double.parseDouble(value);
                    break;
                default:
                    break;
            }
        }
        return config;
    }
}
