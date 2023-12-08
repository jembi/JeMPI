package org.jembi.jempi.linker.thresholdRangeProcessor.lib.muLib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MuModel {

    private HashMap<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap = new HashMap<>();
    private String linkerId;
    private String kafkaBootstrapServer;
    public MuModel(String linkerId, HashMap<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap, String kafkaBootstrapServer) {
        this.linkerId = linkerId;
        this.fieldEqulityPairMatchMap = fieldEqulityPairMatchMap;
        this.kafkaBootstrapServer = kafkaBootstrapServer;
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
        MuAccesor.GetKafkaMUUpdater(this.linkerId, this.kafkaBootstrapServer).updateValue(fieldEqulityPairMatchMap);
    }
    public static MuModel fromDemographicData(String linkerId, Map<String, String> demographicData, String kafkaBootstrapServer) {
        HashMap<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap = new HashMap<>();

        for (String field: demographicData.keySet()){
            fieldEqulityPairMatchMap.put(field, new FieldEqualityPairMatchMatrix());
        }
        return new MuModel(linkerId, fieldEqulityPairMatchMap, kafkaBootstrapServer);
    }
    public HashMap<String, FieldEqualityPairMatchMatrix> getFieldEqualityPairMatchMatrix(){
        return this.fieldEqulityPairMatchMap;
    }

    @Override
    public String toString(){
        StringBuilder matrixData = new StringBuilder("----- Field Data -----");
        for (Map.Entry<String, FieldEqualityPairMatchMatrix> fieldInfo: this.fieldEqulityPairMatchMap.entrySet()){
            matrixData.append(String.format("\n-> %s", fieldInfo.getKey()));
            matrixData.append(fieldInfo.getValue().toString());
        }
        return matrixData.toString();
    }
}
