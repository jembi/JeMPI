package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MuModel {

    private HashMap<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap = new HashMap<>();
    private String linkerId;
    public MuModel(String linkerId, HashMap<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap) {
        this.linkerId = linkerId;
        this.fieldEqulityPairMatchMap = fieldEqulityPairMatchMap;
    }

    public void updateFieldEqualityPairMatchMatrix(String field, Boolean fieldsEqual, Boolean pairMatch){
        FieldEqualityPairMatchMatrix matrix = this.fieldEqulityPairMatchMap.get(field);
        if (pairMatch){
            if (fieldsEqual){
                matrix.updateFieldEqualPairMatch(1);
            }
            else{
                matrix.updateFieldNotEqualPairMatch(1);
            }
        }
        else{
            if (fieldsEqual){
                matrix.updateFieldEqualPairNoMatch(1);
            }
            else{
                matrix.updateFieldNotEqualPairNoMatch(1);
            }
        }

    }
    public void saveToKafka() throws ExecutionException, InterruptedException {
        MuAccesor.GetKafkaMUUpdater(this.linkerId).updateValue(fieldEqulityPairMatchMap);
    }
    public static MuModel fromDemographicData(String linkerId, Map<String, String> demographicData) {
        HashMap<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap = new HashMap<>();

        for (String field: demographicData.keySet()){
            fieldEqulityPairMatchMap.put(field, new FieldEqualityPairMatchMatrix());
        }
        return new MuModel(linkerId, fieldEqulityPairMatchMap);
    }
    public HashMap<String, FieldEqualityPairMatchMatrix> getFieldEqualityPairMatchMatrix(){
        return this.fieldEqulityPairMatchMap;
    }

}
