package org.jembi.jempi.linker.threshold_range_processor.lib.mu_lib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MuModel {

    private HashMap<String, FieldEqualityPairMatchMatrix> fieldEqualityPairMatchMap = new HashMap<>();
    private final String linkerId;
    private final String kafkaBootstrapServer;
    public MuModel(String linkerId, Map<String, FieldEqualityPairMatchMatrix> fieldEqulityPairMatchMap, String kafkaBootstrapServer) {
        this.linkerId = linkerId;
        this.fieldEqualityPairMatchMap = (HashMap<String, FieldEqualityPairMatchMatrix>) fieldEqulityPairMatchMap;
        this.kafkaBootstrapServer = kafkaBootstrapServer;
    }

    public void updateFieldEqualityPairMatchMatrix(String field, Boolean fieldsEqual, Boolean pairMatch){
        FieldEqualityPairMatchMatrix matrix = this.fieldEqualityPairMatchMap.get(field);
        if (Boolean.TRUE.equals(pairMatch)){
            if (Boolean.TRUE.equals(fieldsEqual)){
                matrix.updateFieldEqualPairMatch(1);
            }
            else{
                matrix.updateFieldNotEqualPairMatch(1);
            }
        }
        else{
            if (Boolean.TRUE.equals(fieldsEqual)){
                matrix.updateFieldEqualPairNoMatch(1);
            }
            else{
                matrix.updateFieldNotEqualPairNoMatch(1);
            }
        }
    }
    public void saveToKafka() throws ExecutionException, InterruptedException {
        MuAccesor.getKafkaMUUpdater(this.linkerId, this.kafkaBootstrapServer).updateValue(fieldEqualityPairMatchMap);
    }
    public static MuModel fromDemographicData(String linkerId, Map<String, String> demographicData, String kafkaBootstrapServer) {
        HashMap<String, FieldEqualityPairMatchMatrix> fieldEqualityPairMatchMap = new HashMap<>();

        for (String field: demographicData.keySet()){
            fieldEqualityPairMatchMap.put(field, new FieldEqualityPairMatchMatrix());
        }
        return new MuModel(linkerId, fieldEqualityPairMatchMap, kafkaBootstrapServer);
    }
    public Map<String, FieldEqualityPairMatchMatrix> getFieldEqualityPairMatchMatrix(){
        return this.fieldEqualityPairMatchMap;
    }

    @Override
    public String toString(){
        StringBuilder matrixData = new StringBuilder("----- Field Data -----");
        for (Map.Entry<String, FieldEqualityPairMatchMatrix> fieldInfo: this.fieldEqualityPairMatchMap.entrySet()){
            matrixData.append(String.format("\n-> %s", fieldInfo.getKey()));
            matrixData.append(fieldInfo.getValue().toString());
        }
        return matrixData.toString();
    }
}
