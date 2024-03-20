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
    }

    public LinkConfig(Boolean reporting, Integer computing, Double leftMargin, Double threshold, Double rightMargin,
            Double windowSize) {
        this.reporting = reporting;
        this.computing = computing;
        this.leftMargin = leftMargin;
        this.threshold = threshold;
        this.rightMargin = rightMargin;
        this.windowSize = windowSize;
    }

    public Boolean getReporting() {
        return reporting;
    }

    public void setReporting(Boolean reporting) {
        this.reporting = reporting;
    }

    public Integer getComputing() {
        return computing;
    }

    public void setComputing(Integer computing) {
        this.computing = computing;
    }

    public Double getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(Double leftMargin) {
        this.leftMargin = leftMargin;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Double getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(Double rightMargin) {
        this.rightMargin = rightMargin;
    }

    public Double getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Double windowSize) {
        this.windowSize = windowSize;
    }

    public static LinkConfig fromString(String str) {
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
            }
        }
        return config;
    }
}
