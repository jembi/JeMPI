package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

import java.util.Map;

public class FieldEqualityPairMatchMatrix {
    public FieldEqualityPairMatchMatrix(){}

    public FieldEqualityPairMatchMatrix(int fieldEqualPairMatch, int fieldNotEqualPairMatch, int fieldEqualPairNoMatch, int fieldNotEqualPairNoMatch){
        this.fieldEqualPairMatch = fieldEqualPairMatch;
        this.fieldNotEqualPairMatch = fieldNotEqualPairMatch;
        this.fieldEqualPairNoMatch = fieldEqualPairNoMatch;
        this.fieldNotEqualPairNoMatch = fieldNotEqualPairNoMatch;
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

    public void updateFieldEqualPairMatch(int value){
        fieldEqualPairMatch += value;
    }

    public void updateFieldEqualPairNoMatch(int value){
        fieldEqualPairNoMatch += value;
    }

    public void updateFieldNotEqualPairMatch(int value){
        fieldNotEqualPairMatch += value;
    }

    public void updateFieldNotEqualPairNoMatch(int value){
        fieldNotEqualPairNoMatch += value;
    }
    public FieldEqualityPairMatchMatrix merge(FieldEqualityPairMatchMatrix one, FieldEqualityPairMatchMatrix two){
        return new FieldEqualityPairMatchMatrix(one.getFieldEqualPairMatch() + two.getFieldEqualPairMatch(),
                                                one.getFieldNotEqualPairMatch() + two.getFieldNotEqualPairMatch(),
                                                     one.getFieldEqualPairNoMatch() + two.getFieldEqualPairNoMatch(),
                                                     one.getFieldNotEqualPairNoMatch() + two.getFieldNotEqualPairNoMatch());
    }
    @Override
    public String toString(){

        return "\n" +
                String.format("| ------------------------------------------|\n") +
                String.format("|               | Pair Match | Pair Unmatch |\n") +
                String.format("| ------------------------------------------|\n") +
                String.format("| Field Equal   |     %s     |      %s      |\n", this.fieldEqualPairMatch, this.fieldEqualPairNoMatch) +
                String.format("| Field Unequal |     %s     |      %s      |\n", this.fieldNotEqualPairMatch, this.fieldNotEqualPairNoMatch) +
                String.format("| ------------------------------------------|\n") +
                "\n";

    }
}
