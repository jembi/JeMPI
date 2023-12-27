package org.jembi.jempi.shared.libs.m_and_u;

public final class FieldEqualityPairMatchMatrix {

    public FieldEqualityPairMatchMatrix() { }
    public FieldEqualityPairMatchMatrix(final int fieldEqualPairMatchIn, final int fieldNotEqualPairMatchIn, final int fieldEqualPairNoMatchIn, final int fieldNotEqualPairNoMatchIn) {
        this.fieldEqualPairMatch = fieldEqualPairMatchIn;
        this.fieldNotEqualPairMatch = fieldNotEqualPairMatchIn;
        this.fieldEqualPairNoMatch = fieldEqualPairNoMatchIn;
        this.fieldNotEqualPairNoMatch = fieldNotEqualPairNoMatchIn;
    }
    private int fieldEqualPairMatch = 0;
    private int fieldNotEqualPairMatch = 0;
    private int fieldEqualPairNoMatch = 0;
    private int fieldNotEqualPairNoMatch = 0;

    public int getFieldEqualPairMatch() {
        return fieldEqualPairMatch;
    }

    public int getFieldEqualPairNoMatch() {
        return fieldEqualPairNoMatch;
    }

    public int getFieldNotEqualPairMatch() {
        return fieldNotEqualPairMatch;
    }

    public int getFieldNotEqualPairNoMatch() {
        return fieldNotEqualPairNoMatch;
    }

    public void updateFieldEqualPairMatch(final int value) {
        fieldEqualPairMatch += value;
    }

    public void updateFieldEqualPairNoMatch(final int value) {
        fieldEqualPairNoMatch += value;
    }

    public void updateFieldNotEqualPairMatch(final int value) {
        fieldNotEqualPairMatch += value;
    }

    public void updateFieldNotEqualPairNoMatch(final int value) {
        fieldNotEqualPairNoMatch += value;
    }
    public FieldEqualityPairMatchMatrix merge(final FieldEqualityPairMatchMatrix one, final FieldEqualityPairMatchMatrix two) {
        return new FieldEqualityPairMatchMatrix(one.getFieldEqualPairMatch() + two.getFieldEqualPairMatch(),
                                                one.getFieldNotEqualPairMatch() + two.getFieldNotEqualPairMatch(),
                                                     one.getFieldEqualPairNoMatch() + two.getFieldEqualPairNoMatch(),
                                                     one.getFieldNotEqualPairNoMatch() + two.getFieldNotEqualPairNoMatch());
    }
    @Override
    public String toString() {

        return "\n"
                + "| ------------------------------------------|\n"
                + "|               | Pair Match | Pair Unmatch |\n"
                + "| ------------------------------------------|\n"
                + String.format("| Field Equal   |     %s     |      %s      |\n", this.fieldEqualPairMatch, this.fieldEqualPairNoMatch)
                + String.format("| Field Unequal |     %s     |      %s      |\n", this.fieldNotEqualPairMatch, this.fieldNotEqualPairNoMatch)
                + String.format("| ------------------------------------------|\n") + "\n";

    }
}
