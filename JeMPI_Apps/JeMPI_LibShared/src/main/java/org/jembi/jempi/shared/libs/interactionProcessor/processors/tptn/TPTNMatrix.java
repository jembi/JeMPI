package org.jembi.jempi.shared.libs.interactionProcessor.processors.tptn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TPTNMatrix {

    public TPTNMatrix() { }
    public TPTNMatrix(final long truePositive, final long trueNegative, final long falsePositive, final long falseNegative) {
        this.truePositive = truePositive;
        this.trueNegative = trueNegative;
        this.falsePositive = falsePositive;
        this.falseNegative = falseNegative;
    }
    private long truePositive = 0;
    private long trueNegative = 0;
    private long falsePositive = 0;
    private long falseNegative = 0;

    public long getTruePositive() {
        return truePositive;
    }

    public long getTrueNegative() {
        return trueNegative;
    }

    public long getFalsePositive() {
        return falsePositive;
    }

    public long getFalseNegative() {
        return falseNegative;
    }

    public void updateTruePositive(final long value) {
        truePositive += value;
    }

    public void updateTrueNegative(final long value) {
        trueNegative += value;
    }

    public void updateFalsePositive(final long value) {
        falsePositive += value;
    }

    public void updateFalseNegative(final long value) {
        falseNegative += value;
    }
    public TPTNMatrix merge(final TPTNMatrix one, final TPTNMatrix two) {
        return new TPTNMatrix(one.getTruePositive() + two.getTruePositive(),
                one.getTrueNegative() + two.getTrueNegative(),
                one.getFalsePositive() + two.getFalsePositive(),
                one.getFalseNegative() + two.getFalseNegative());
    }

    @Override
    public String toString() {

        return "\n"
                + "| ------------------------------------------|\n"
                + "|             TP / TP / FP / FN              |\n"
                + "| ------------------------------------------|\n"
                + String.format("| True Positive  |     %s     |\n", this.truePositive)
                + String.format("| True Negative  |     %s     |\n", this.trueNegative)
                + String.format("| False Postive  |     %s     |\n", this.falsePositive)
                + String.format("| False Negative |     %s     |\n", this.falseNegative)
                + String.format("| ----------------------------\n") + "\n";

    }


}
