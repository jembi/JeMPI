package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

public class FieldEqualityPairMatchMatrix {
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
}
